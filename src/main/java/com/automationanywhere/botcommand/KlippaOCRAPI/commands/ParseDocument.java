package com.automationanywhere.botcommand.KlippaOCRAPI.commands;

import static com.automationanywhere.commandsdk.model.DataType.STRING;

import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.exception.BotCommandException;

import com.automationanywhere.commandsdk.annotations.BotCommand;
import com.automationanywhere.commandsdk.annotations.CommandPkg;
import com.automationanywhere.commandsdk.annotations.Idx;
import com.automationanywhere.commandsdk.annotations.Pkg;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;
import com.automationanywhere.commandsdk.annotations.Execute;

import com.automationanywhere.core.security.SecureString;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@BotCommand
@CommandPkg(name = "parsedocument", label = "[[ParseDocument.label]]",
        description = "[[ParseDocument.description]]",
        node_label = "[[ParseDocument.node_label]]", icon = "klippa.svg", comment = true,
        return_type=DataType.DICTIONARY, return_label="[[ParseDocument.return_label]]", return_required=true)
public class ParseDocument {
    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.KlippaOCRAPI.messages");
    private static final Logger logger = LogManager.getLogger(ParseDocument.class);

    @Execute
    public DictionaryValue action(
            @Idx(index = "1", type = AttributeType.TEXT) @Pkg(label = "[[ParseDocument.BasePath.label]]", description = "[[ParseDocument.BasePath.description]]", default_value_type = STRING, default_value = "https://custom-ocr.klippa.com/api/v1") @NotEmpty String BasePath,
            @Idx(index = "2", type = AttributeType.CREDENTIAL) @Pkg(label = "[[ParseDocument.APIKey.label]]", description = "[[ParseDocument.APIKey.label]]") @NotEmpty SecureString APIKey,
            @Idx(index = "3", type = AttributeType.TEXT) @Pkg(label = "[[ParseDocument.DocumentURL.label]]", description = "[[ParseDocument.DocumentURL.label]]") String DocumentURL,
            @Idx(index = "4", type = AttributeType.FILE) @Pkg(label = "[[ParseDocument.DocumentPath.label]]", description = "[[ParseDocument.DocumentPath.label]]") String DocumentPath,
            @Idx(index = "5", type = AttributeType.TEXT) @Pkg(label = "[[ParseDocument.Template.label]]", description = "[[ParseDocument.Template.label]]") String Template,
            @Idx(index = "6", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "6.1", pkg = @Pkg(label = "[[ParseDocument.PDFTextExtraction.fast.label]]", value = "fast")),
                    @Idx.Option(index = "6.2", pkg = @Pkg(label = "[[ParseDocument.PDFTextExtraction.full.label]]", value = "full")),
            }) @Pkg(label = "[[ParseDocument.PDFTextExtraction.label]]", description = "[[ParseDocument.PDFTextExtraction.label]]", default_value = "full", default_value_type = STRING) String PDFTextExtraction,
            @Idx(index = "7", type = AttributeType.TEXT) @Pkg(label = "[[ParseDocument.UserData.label]]", description = "[[ParseDocument.UserData.label]]", default_value_type = STRING) String UserData,
            @Idx(index = "8", type = AttributeType.TEXT) @Pkg(label = "[[ParseDocument.UserDataSetExternalID.label]]", description = "[[ParseDocument.UserDataSetExternalID.label]]", default_value_type = STRING) String UserDataSetExternalID,
            @Idx(index = "9", type = AttributeType.TEXT) @Pkg(label = "[[ParseDocument.HashDuplicateGroupID.label]]", description = "[[ParseDocument.HashDuplicateGroupID.label]]", default_value_type = STRING) String HashDuplicateGroupID
    ) throws BotCommandException
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(BasePath + "/parseDocument");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        if ((DocumentPath == null || DocumentPath.equalsIgnoreCase("")) && (DocumentURL == null || DocumentURL.equalsIgnoreCase(""))) {
            throw new BotCommandException(MESSAGES.getString("noDocument", "DocumentPath", "DocumentURL"));
        }

        if ((DocumentPath != null && !DocumentPath.equalsIgnoreCase("")) && (DocumentURL != null && !DocumentURL.equalsIgnoreCase(""))) {
            throw new BotCommandException(MESSAGES.getString("twoDocuments", "DocumentPath", "DocumentURL"));
        }

        if (DocumentPath != null && !DocumentPath.equalsIgnoreCase("")) {
            logger.info("Setting document path: {}", DocumentPath);
            try {
                // This attaches TheFile to the POST:
                File f = new File(DocumentPath);
                builder.addBinaryBody(
                        "document",
                        new FileInputStream(f),
                        ContentType.MULTIPART_FORM_DATA,
                        f.getName()
                );
            }
            catch (IOException e) {
                throw new BotCommandException(MESSAGES.getString("errorReadingFile", DocumentPath, e.getLocalizedMessage()));
            }
        }

        if (DocumentURL != null && !DocumentURL.equalsIgnoreCase("")) {
            logger.info("Setting document url: {}", DocumentURL);

            builder.addTextBody(
                    "url",
                    DocumentURL
            );
        }

        if (Template != null && !Template.equalsIgnoreCase("")) {
            logger.info("Setting document template: {}", Template);

            builder.addTextBody(
                    "template",
                    Template
            );
        }

        if (PDFTextExtraction != null && !PDFTextExtraction.equalsIgnoreCase("")) {
            logger.info("Setting document pdf text extraction: {}", PDFTextExtraction);

            builder.addTextBody(
                    "pdf_text_extraction",
                    PDFTextExtraction
            );
        }

        if (UserData != null && !UserData.equalsIgnoreCase("")) {
            logger.debug("Setting document user data: {}", UserData);

            builder.addTextBody(
                    "user_data",
                    UserData
            );
        }

        if (UserDataSetExternalID != null && !UserDataSetExternalID.equalsIgnoreCase("")) {
            logger.debug("Setting document user data external id: {}", UserDataSetExternalID);

            builder.addTextBody(
                    "user_data_set_external_id",
                    UserDataSetExternalID
            );
        }

        if (HashDuplicateGroupID != null && !HashDuplicateGroupID.equalsIgnoreCase("")) {
            logger.debug("Setting document hash duplicate group ID: {}", HashDuplicateGroupID);

            builder.addTextBody(
                    "hash_duplicate_group_id",
                    HashDuplicateGroupID
            );
        }

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        uploadFile.setHeader("X-Auth-Key", APIKey.getInsecureString());

        String responseJSON = "";
        try {
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            responseJSON = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());

            logger.debug("API Response: {}", responseJSON);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new BotCommandException(MESSAGES.getString("errorFromOCRAPI", responseJSON));
            }
        }
        catch (IOException e) {
            throw new BotCommandException(MESSAGES.getString("errorReadingAPIResponse", e.getLocalizedMessage()));
        }

        JSONUtils parser = new JSONUtils();
        return parser.parseJSON(responseJSON);
    }
}