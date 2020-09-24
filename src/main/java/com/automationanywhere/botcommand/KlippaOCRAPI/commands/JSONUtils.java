package com.automationanywhere.botcommand.KlippaOCRAPI.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.BooleanValue;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.ListValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;

public class JSONUtils {


    private DictionaryValue parseJSONObj(JSONObject jsonobj) {
        Map map = new HashMap<String, Value>();

        for (Iterator<String> iterator = jsonobj.keys(); iterator.hasNext(); ) {
            String key = iterator.next();
            Object obj = jsonobj.get(key);
            String classname = obj.getClass().getSimpleName();

            switch (classname) {
                case "String":
                    map.put(key, new StringValue(obj));
                    break;
                case "Integer":
                    map.put(key, new NumberValue(obj));
                    break;
                case "Double":
                    map.put(key, new NumberValue(obj));
                    break;
                case "Boolean":
                    map.put(key, new BooleanValue(obj));
                    break;
                case "JSONObject":
                    map.put(key, parseJSONObj((JSONObject) obj));
                    break;
                case "JSONArray":
                    map.put(key, parseJSONArray((JSONArray) obj));
                    break;
                default:
                    break;
            }

        }
        DictionaryValue dict = new DictionaryValue();
        dict.set(map);
        return dict;
    }

    private ListValue<Value> parseJSONArray(JSONArray jsonarray) {
        List<Value> list = new ArrayList<Value>();
        for (int i = 0; i < jsonarray.length(); i++) {

            Object obj = jsonarray.get(i);
            String classname = obj.getClass().getSimpleName();
            switch (classname) {
                case "String":
                    list.add(new StringValue(obj));
                    break;
                case "Integer":
                    list.add(new NumberValue(obj));
                    break;
                case "Double":
                    list.add(new NumberValue(obj));
                    break;
                case "Boolean":
                    list.add(new BooleanValue(obj));
                    break;
                case "JSONObject":
                    list.add(parseJSONObj((JSONObject) obj));
                    break;
                case "JSONArray":
                    list.add(parseJSONArray((JSONArray) obj));
                    break;
                default:
                    break;
            }
        }

        ListValue<Value> valuelist = new ListValue<Value>();
        valuelist.set(list);
        return valuelist;
    }

    public DictionaryValue parseJSON(String jsonstring) {
        JSONObject json = new JSONObject(jsonstring);
        DictionaryValue map = parseJSONObj(json);
        return map;
    }

    public DictionaryValue queryJSON(String jsonstring, String query) {

        DictionaryValue dict = new DictionaryValue();
        Map map = new HashMap<String, Value>();
        String key = query.substring(query.lastIndexOf(".") + 1, query.length());
        JSONObject json = new JSONObject(jsonstring);
        Object obj = json.query("/" + query.replaceAll("\\.", "/"));

        if (obj != null) {

            String classname = obj.getClass().getSimpleName();
            switch (classname) {
                case "String":
                    map.put(key, new StringValue(obj));
                    break;
                case "Integer":
                    map.put(key, new NumberValue(obj));
                    break;
                case "Double":
                    map.put(key, new NumberValue(obj));
                    break;
                case "Boolean":
                    map.put(key, new BooleanValue(obj));
                    break;
                case "JSONObject":
                    map.put(key, parseJSONObj((JSONObject) obj));
                    break;
                case "JSONArray":
                    map.put(key, parseJSONArray((JSONArray) obj));
                    break;
                default:
                    break;
            }
        }

        dict.set(map);
        return dict;
    }

    private JSONArray parseList(List<Value> list) {
        JSONArray jsonarray = new JSONArray();
        for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            Value value = (Value) iterator.next();

            String classname = value.getClass().getSimpleName();
            switch (classname) {
                case "StringValue":
                    jsonarray.put(((StringValue) value).get());
                    break;
                case "NumberValue":
                    NumberValue nvalue = (NumberValue) value;
                    if (isDouble(nvalue)) {
                        jsonarray.put(((NumberValue) value).get());
                    } else {
                        jsonarray.put(((NumberValue) value).get().intValue());
                    }
                    break;
                case "BooleanValue":
                    jsonarray.put(((BooleanValue) value).get());
                    break;
                case "DictionaryValue":
                    Map<String, Value> values = ((DictionaryValue) value).get();
                    JSONObject subobj = parseValue(values);
                    jsonarray.put(subobj);
                    break;
                case "ListValue":
                    List<Value> sublist = ((ListValue) value).get();
                    JSONArray subarray = parseList(sublist);
                    jsonarray.put(sublist);
                    break;
                default:
                    break;

            }
        }

        return jsonarray;
    }


    private JSONObject parseValue(Map<String, Value> map) {
        JSONObject jsonobj = new JSONObject();

        for (Entry<String, Value> value : map.entrySet()) {
            String classname = value.getValue().getClass().getSimpleName();

            switch (classname) {
                case "StringValue":
                    jsonobj.put(value.getKey(), ((StringValue) value.getValue()).get());
                    break;
                case "NumberValue":
                    NumberValue nvalue = (NumberValue) value.getValue();
                    if (isDouble(nvalue)) {
                        jsonobj.put(value.getKey(), nvalue.get());
                    } else {
                        jsonobj.put(value.getKey(), nvalue.get().intValue());
                    }
                    break;
                case "BooleanValue":
                    jsonobj.put(value.getKey(), ((BooleanValue) value.getValue()).get());
                    break;
                case "DictionaryValue":
                    Map<String, Value> values = ((DictionaryValue) value.getValue()).get();
                    JSONObject subobj = parseValue(values);
                    jsonobj.put(value.getKey(), subobj);
                    break;
                case "ListValue":
                    List<Value> list = ((ListValue) value.getValue()).get();
                    JSONArray jsonarray = parseList(list);
                    jsonobj.put(value.getKey(), jsonarray);
                    break;
                default:
                    break;
            }
        }

        return jsonobj;
    }

    public String toJSON(Map<String, Value> value) {
        String jsonString = "";
        JSONObject jsonObject = parseValue(value);
        return jsonObject.toString();
    }

    public boolean isDouble(NumberValue value) {
        Double dvalue = value.get();
        Integer ivalue = dvalue.intValue();
        Double diff = dvalue - Double.valueOf(ivalue);
        return (diff != 0.0000000000);
    }
}