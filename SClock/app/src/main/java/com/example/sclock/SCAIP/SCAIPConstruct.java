package com.example.sclock.SCAIP;

import android.util.Log;
import android.util.Xml;

import com.example.sclock.basedatos.*;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;


public class SCAIPConstruct {


    private String reference;
    private String version;
    private String controller_id;
    private String device_type;

    private String type;
    private Schema schema;
    private MiBaseDatos MDB;

    private String request;
    ArrayList<String> ordenMrq = new ArrayList<String>(Arrays.asList(
            "ref","ver","sco","cha","mty","hbo","cid","dty","did","dco","dte","crd","stc","stt","pri","lco","lva","lge","lte","ico","ite","ame"));
    private String response;
    ArrayList<String> ordenMrs = new ArrayList<String>(Arrays.asList(
            "ref","snu","ste","cve","mre","cre","tnu","hbi"));



    public void MDBinsert(MiBaseDatos MDB){
        this.MDB=MDB;
    }

    public SCAIPConstruct(String version, String controller_id, String device_type, Schema schema){
        this.version=version;
        this.controller_id=controller_id;
        this.device_type=device_type;
        this.schema=schema;
    }

    public String CreateRequest(String type){

        HashMap<String,String> argumentos = new HashMap<String,String>();
        request = CreateRequest(type,argumentos);
        return request;
    }

    public String CreateRequest(String type, HashMap arguments){


        try{
            this.type = type;
            Plantilla plantilla = MDB.recuperarPlantillas("nombreAlarma='" + type + "'").get(0);
            ArrayList<String> argumentosPlantilla = MDB.stringDecod(plantilla.getArgumentos());
            ArrayList<String> argumentosSetPlantilla = MDB.stringDecod(plantilla.getArgumentosSet());
            for(String argumento: argumentosSetPlantilla) {
                String[] partes = argumento.split("=");
                arguments.put(partes[0],partes[1]);
            }

            for(String argumento: argumentosPlantilla){
                if(arguments.get(argumento)==null) {
                    try {
                        arguments.put(argumento, argumentoGeneral(argumento));
                    } catch (Exception ex) {
                        Log.e("SCAIPConstruct", String.valueOf(ex));
                    }
                }
            }
            request = CreateXML(arguments, ordenMrq,"mrq");

        }catch(Exception e){
            Log.e("SCAIPConstruct", String.valueOf(e));
        }
        return request;
    }

    public String CreateXML(HashMap arguments, ArrayList<String> order, String type){


        String XML = null;
        //Construye en orden el XML SCAIP con los argumentos recibidos
        try {
            XmlSerializer serializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            serializer.setOutput(writer);
            if(type.equals("mrq")) {
                serializer.startTag(null, "mrq");
            }else if(type.equals("mrs")){
                serializer.startTag(null, "mrs");
            }
            for (String value: order) {
                if (value == "lge" && arguments.get(value)!=null) {
                    serializer.startTag(null, "lge");

                    serializer.startTag(null, "geo");
                    serializer.text((String) arguments.get("geo"));
                    serializer.endTag(null, "geo");

                    serializer.startTag(null, "tim");
                    serializer.text((String) arguments.get("tim"));
                    serializer.endTag(null, "tim");

                    serializer.startTag(null, "gga");
                    serializer.text((String) arguments.get("gga"));
                    serializer.endTag(null, "gga");

                    serializer.endTag(null, "lge");
                } else if(arguments.get(value)!=null){
                    serializer.startTag(null, value);
                    serializer.text((String) arguments.get(value));
                    serializer.endTag(null, value);
                }
            }

            if(type.equals("mrq")) {
                serializer.endTag(null, "mrq");
            }else if(type.equals("mrs")){
                serializer.endTag(null, "mrs");
            }

            serializer.endDocument();

            XML = writer.toString();
            Log.d("XML", XML);
        } catch (Exception e) {
            Log.e("SCAIPConstruct", String.valueOf(e));
        }
        Boolean bool = SCAIPConstruct.validarXML(XML,schema);
        Log.i("SCAIPConstruct", String.valueOf(bool));

        return XML;
    }

    public String argumentoGeneral(String argumento){

        String argumentoGeneral = null;
        switch (argumento){
            case "ref":
                Random random = new Random();
                int numeroAleatorio = random.nextInt(900) + 100;
                reference = type.substring(0,3) + numeroAleatorio;
                argumentoGeneral = reference;
                break;
            case "ver" :
                argumentoGeneral = version;
                break;
            case "cid":
                argumentoGeneral = controller_id;
                break;
            case "dty":
                argumentoGeneral = device_type;
                break;
            default:
                Log.e("Error", "Error");
        }
        return argumentoGeneral;
    }


    public static boolean validarXML(String xmlString, Schema schema) {

        // Valida que el XML concuerda con el schema SCAIP
        try {
            Source xmlSource = new StreamSource(new StringReader(xmlString));
            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
        } catch (Exception e) {
            Log.e("Validator", String.valueOf(e));
            return false;
        }

        return true;
    }

    public String CreateResponse(HashMap arguments){


        try{
            arguments.put("snu", "0");
            response = CreateXML(arguments, ordenMrs,"mrs");

        }catch(Exception e){
            Log.e("SCAIPConstruct", String.valueOf(e));
        }
        return response;
    }

}