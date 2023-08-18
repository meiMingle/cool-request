package com.hxl.plugin.springboot.invoke.view.page;

import com.hxl.plugin.springboot.invoke.invoke.ControllerInvoke;
import com.hxl.plugin.springboot.invoke.net.FormDataInfo;
import com.hxl.plugin.springboot.invoke.net.KeyValue;
import com.hxl.plugin.springboot.invoke.net.MapRequest;
import com.hxl.plugin.springboot.invoke.utils.UrlUtils;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RequestBodyPage extends JPanel implements MapRequest {
    private static final Map<String, ContentTypeConvert> CONTENT_TYPE_MAP = new HashMap<>();
    private final Project project;
    private final Map<String, JRadioButton> radioButtons = new HashMap<>();
    private JSONRequestBodyPage jsonRequestBodyPage;
    private XmlParamRequestBodyPage xmlParamRequestBodyPage;
    private RawParamRequestBodyPage rawParamRequestBodyPage;
    private BinaryRequestBodyPage binaryRequestBodyPage;
    private FormDataRequestBodyPage formDataRequestBodyPage;
    private CardLayout cardLayout;
    private JPanel contentPageJPanel;
    private FormUrlencodedRequestBodyPage urlencodedRequestBodyPage;

    {
        CONTENT_TYPE_MAP.put("form-data", new FormDataContentTypeConvert());
        CONTENT_TYPE_MAP.put("x-www-form-urlencoded", new FormUrlEncodedContentTypeConvert());
        CONTENT_TYPE_MAP.put("json", new ApplicationJSONContentTypeConvert());
        CONTENT_TYPE_MAP.put("xml", new XmlContentTypeConvert());
        CONTENT_TYPE_MAP.put("raw", new RawContentTypeConvert());
    }


    public RequestBodyPage(Project project) {
        this.project = project;
        init();
    }

    private JRadioButton createJRadioButton(String name, Consumer<String> callable) {
        JRadioButton jRadioButton = new JRadioButton(name);
        jRadioButton.addActionListener(e -> callable.consume(name));
        return jRadioButton;
    }

    @Override
    public void configRequest(ControllerInvoke.ControllerRequestData controllerRequestData) {
        //设置content-type
        String selectType = "";
        for (String key : radioButtons.keySet()) {
            if (radioButtons.get(key).isSelected()) {
                selectType = key;
                ContentTypeConvert contentTypeConvert = CONTENT_TYPE_MAP.get(key);
                controllerRequestData.setContentType(contentTypeConvert.getContentType());
                break;
            }
        }
        controllerRequestData.setBody(CONTENT_TYPE_MAP.getOrDefault(selectType, new ContentTypeConvert() {
            @Override
            public String getContentType() {
                return "text/paint";
            }

            @Override
            public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData) {
                return "";
            }
        }).getBody(controllerRequestData));
    }

    public void init() {
        setLayout(new BorderLayout());
        jsonRequestBodyPage = new JSONRequestBodyPage(this.project);
        xmlParamRequestBodyPage = new XmlParamRequestBodyPage(this.project);
        rawParamRequestBodyPage = new RawParamRequestBodyPage(this.project);
        binaryRequestBodyPage = new BinaryRequestBodyPage(this.project);
        urlencodedRequestBodyPage = new FormUrlencodedRequestBodyPage();
        formDataRequestBodyPage= new FormDataRequestBodyPage();
        Map<String, JPanel> pageMap = new HashMap<>();

        List<String> sortParam = Arrays.asList("form-data", "x-www-form-urlencoded", "json", "xml", "raw", "binary");

        pageMap.put("form-data", formDataRequestBodyPage);
        pageMap.put("x-www-form-urlencoded", urlencodedRequestBodyPage);
        pageMap.put("json", jsonRequestBodyPage);
        pageMap.put("xml", xmlParamRequestBodyPage);
        pageMap.put("raw", rawParamRequestBodyPage);
        pageMap.put("binary", binaryRequestBodyPage);

        JPanel topJPanel = new JPanel();
         contentPageJPanel = new JPanel();
         cardLayout = new CardLayout();

        contentPageJPanel.setLayout(cardLayout);


        ButtonGroup buttonGroup = new ButtonGroup();
        Consumer<String> radioButtonConsumer = s -> {
            cardLayout.show(contentPageJPanel, s);
        };
        for (String paramName : sortParam) {
            JRadioButton jRadioButton = createJRadioButton(paramName, radioButtonConsumer);
            radioButtons.put(paramName, jRadioButton);
            buttonGroup.add(jRadioButton);
            topJPanel.add(jRadioButton);
            contentPageJPanel.add(paramName, pageMap.get(paramName));
        }
        setRequestBodyType("json");
        buttonGroup.setSelected(radioButtons.get("json").getModel(),true);
        topJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(topJPanel, BorderLayout.NORTH);
        add(contentPageJPanel, BorderLayout.CENTER);
    }

    public String getSelectedRequestBodyType() {
        for (String name : radioButtons.keySet()) {
            if (radioButtons.get(name).isSelected()) return name;
        }
        return "";
    }

    public List<FormDataInfo> getFormDataInfo() {
        return formDataRequestBodyPage.getFormData();
    }
    public List<KeyValue> getUrlencodedBody() {
        return urlencodedRequestBodyPage.getTableMap();
    }
    public String getTextRequestBody() {
        if (radioButtons.get("json").isSelected()) return jsonRequestBodyPage.getText();
        if (radioButtons.get("xml").isSelected()) return xmlParamRequestBodyPage.getText();
        if (radioButtons.get("raw").isSelected()) return rawParamRequestBodyPage.getText();
        return "";
    }

    public void setRequestBodyType(String requestBodyType) {
        if (!radioButtons.containsKey(requestBodyType)) return;
        radioButtons.get(requestBodyType).setSelected(true);
        cardLayout.show(contentPageJPanel, requestBodyType);
    }

    public void setJsonBodyText(String textBody) {
        jsonRequestBodyPage.setText(textBody);
    }

    public void setXmlBodyText(String textBody) {
        xmlParamRequestBodyPage.setText(textBody);
    }

    public void setRawBodyText(String textBody) {
        rawParamRequestBodyPage.setText(textBody);
    }

    public void setUrlencodedBodyTableData(List<KeyValue> urlencodedBody) {
        urlencodedRequestBodyPage.setTableData(urlencodedBody);
    }

    public void setFormData(List<FormDataInfo> formDataInfos) {
        formDataRequestBodyPage.setFormData(formDataInfos);
    }

    interface ContentTypeConvert {
        public String getContentType();

        public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData);
    }

    class ApplicationJSONContentTypeConvert implements ContentTypeConvert {
        @Override
        public String getContentType() {
            return "application/json";
        }

        @Override
        public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData) {
            return jsonRequestBodyPage.getText();
        }
    }

    class FormUrlEncodedContentTypeConvert implements ContentTypeConvert {
        @Override
        public String getContentType() {
            return "application/x-www-form-urlencoded";
        }

        @Override
        public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData) {
            return UrlUtils.mapToUrlParams(urlencodedRequestBodyPage.getTableMap());
        }
    }

    class XmlContentTypeConvert implements ContentTypeConvert {
        @Override
        public String getContentType() {
            return "application/xml";
        }

        @Override
        public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData) {
            return xmlParamRequestBodyPage.getText();
        }
    }

    class RawContentTypeConvert implements ContentTypeConvert {
        @Override
        public String getContentType() {
            return "text/paint";
        }

        @Override
        public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData) {
            return rawParamRequestBodyPage.getText();
        }
    }
    class FormDataContentTypeConvert implements ContentTypeConvert{
        @Override
        public String getContentType() {
            return "multipart/form-data";
        }

        @Override
        public String getBody(ControllerInvoke.ControllerRequestData controllerRequestData) {
            controllerRequestData.setFormData(formDataRequestBodyPage.getFormData());
            return "";
        }
    }
}