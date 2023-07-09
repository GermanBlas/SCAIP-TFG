package com.example.sserver.SCAIP;

import android.gov.nist.javax.sip.header.CSeq;
import android.gov.nist.javax.sip.header.CallID;
import android.javax.sip.ClientTransaction;
import android.javax.sip.Dialog;
import android.javax.sip.DialogTerminatedEvent;
import android.javax.sip.IOExceptionEvent;
import android.javax.sip.ListeningPoint;
import android.javax.sip.RequestEvent;
import android.javax.sip.ResponseEvent;
import android.javax.sip.ServerTransaction;
import android.javax.sip.SipFactory;
import android.javax.sip.SipListener;
import android.javax.sip.SipProvider;
import android.javax.sip.SipStack;
import android.javax.sip.TimeoutEvent;
import android.javax.sip.TransactionTerminatedEvent;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.address.URI;
import android.javax.sip.header.AuthorizationHeader;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.MaxForwardsHeader;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.header.WWWAuthenticateHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;
import android.util.Log;

import com.example.sserver.*;
import com.example.sserver.basedatos.*;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class SCAIPListener implements SipListener {

    private static SipProvider sipProvider;
    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory headerFactory;
    private static SipStack sipStack;
    private ListeningPoint listeningPoint;
    private ClientTransaction inviteTid;
    private Dialog dialog;
    private ContactHeader contactHeader;

    private boolean byeSeen;
    private boolean enforceTlsPolicyCalled;

    private String name;
    private String ipAddress;
    private String port;
    private String ipAddressProxy;
    private String portProxy;
    private String transportProtocol;
    private String proxyMode;
    private String nameEnd;
    private String ipAddressEnd;
    private String portEnd;


    private Request RegisterSecretRequest;

    private MiBaseDatos MDB;
    private SCAIPWebApplication global;

    public void MDBinsert(MiBaseDatos MDB){
        this.MDB=MDB;
    }
    public void globalInsert(SCAIPWebApplication global){this.global = global;}

    //Inicializamos el Listener (Revisar protocolos transporte)
    public void init(String name, String ipAddress, String port,
                     String ipAddressProxy, String portProxy, String transportProtocol, String proxyMode,
                     String nameEnd, String ipAddressEnd, String portEnd){
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.ipAddressProxy = ipAddressProxy;
        this.portProxy = portProxy;
        this.transportProtocol = transportProtocol;
        this.proxyMode = proxyMode;
        this.nameEnd = nameEnd;
        this.ipAddressEnd = ipAddressEnd;
        this.portEnd = portEnd;

        //Inicialización de la factoria
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("android.gov.nist"); //Este path determina donde estan las implementaciones de la libreria

        //Declaracion de las propiedades
        Properties properties = new Properties();

        //Declaramos el router para los mensajes sip
        if(proxyMode.equals("ON")) {
            properties.setProperty("android.javax.sip.OUTBOUND_PROXY", ipAddressProxy + ":" + portProxy + "/" + transportProtocol);
        }
        properties.setProperty("android.javax.sip.STACK_NAME", name);
        properties.setProperty("android.javax.sip.IP_ADDRESS", ipAddress);

        properties.setProperty("android.gov.nist.javax.sip.ENABLED_CIPHER_SUITES","TLS_RSA_WITH_AES_128_CBC_SHA");
        properties.setProperty("android.gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE", "DisabledAll");

        try {
            //Creamos el stack
            sipStack = sipFactory.createSipStack(properties);

            //Inicializamos las factorias necesarias
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();

            //Creamos un Listening Point
            listeningPoint = sipStack.createListeningPoint(ipAddress, Integer.parseInt(port), transportProtocol);

            //Creamos un sip Provider
            sipProvider = sipStack.createSipProvider(listeningPoint);
            sipProvider.addSipListener(this);
        } catch (Exception e) {
            Log.e("LogListener", "Error: " + e);
        }
    }

    public Request createRequest(String toSipAddress, String toUser, String toSipPort, String requestMethod) {
        Request request = null;

        try {
            // Crea cabecera From
            SipURI fromAddress = addressFactory.createSipURI(name, ipAddress);
            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(name);
            FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345"); //todo esta tag es importante?

            // Crea cabecera To
            SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toUser);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null); //todo tenemos la contraparte

            // Crea RequestURI
            SipURI requestURI = addressFactory.createSipURI(toUser, toSipAddress + ":" + toSipPort);

            // Crea ViaHeaders
            ArrayList viaHeaders = new ArrayList();
            String ipAddress = listeningPoint.getIPAddress();
            String transport = listeningPoint.getTransport();
            ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress, sipProvider.getListeningPoint(transport).getPort(), transport, null);
            viaHeaders.add(viaHeader);

            // Max Forwards Headers
            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, requestMethod);

            // Crea Request
            request = messageFactory.createRequest(requestURI, requestMethod, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

            // Crea cabecera contacto
            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());
            contactURI.setTransportParam(transport);
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(name);
            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);
        }catch (Exception e){
            Log.e("LogListener", name + ": Exception = " + e);
        }
        return request;
    }

    public Request createRequest(String toSipAddress, String toUser, String toSipPort, String requestMethod, String dataMessage){
        Request request = this.createRequest(toSipAddress,toUser,toSipPort,requestMethod);
        try {
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain"); //todo puedo poner lo que me de la gana??
            request.setContent(dataMessage, contentTypeHeader);
        }catch(Exception e){
            Log.e("LogListener", name + ": Exception = " + e);
        }
        return request;
    }

    public Request createRequestProxy(String message, String method){
        Request request = null;
        if(message!="null") {
            request = this.createRequest(ipAddressProxy, name, portProxy, method, message);
        }else {
            request = this.createRequest(ipAddressProxy, name, portProxy, method);
        }
        return request;
    }

    public Request createRequestEnd(String message, String method){
        Request request = null;
        if(message!="null") {
            request = this.createRequest(ipAddressEnd, nameEnd, portEnd, method, message);
        }else {
            request = this.createRequest(ipAddressEnd, nameEnd, portEnd, method);
        }
        return request;
    }


    public void sendRequest(Request request){
        try {
            CallID callIDHeader = (CallID) request.getHeader(CallID.NAME);
            String callID = callIDHeader.getCallId();

            //Crea la transaccion y envia la request
            inviteTid = sipProvider.getNewClientTransaction(request);
            inviteTid.sendRequest();
        }catch (Exception e){
            Log.e("LogListener", name + ": Exception = " + e);
        }
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {

        Request request = requestEvent.getRequest();
        Log.i("LogListener","Request: " + request);
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            processCancel(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.MESSAGE)) {
            processMessage(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.OPTIONS)) {
            processOptions(requestEvent, serverTransactionId);
        } else {
            Log.e("LogListener", "Error en la transaccion NO RESPETA METODOS");
        }

    }

    public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
    }

    public void processAck(RequestEvent requestEvent, ServerTransaction serverTransaction){
        Log.i("LogListener", "LLEGO UN ACK QUE?????");
    }

    public void processBye(RequestEvent requestEvent, ServerTransaction serverTransaction){}

    public void processCancel(RequestEvent requestEvent, ServerTransaction serverTransaction){}

    //todo deberia revisar esto pero funciona joya
    public void processMessage(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            Log.i("LogListener", name + " : Procesando Message");
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }

            Response responseOK = messageFactory.createResponse(Response.OK, request);

            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transportProtocol).getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(name);
            contactHeader = headerFactory.createContactHeader(contactAddress);
            responseOK.addHeader(contactHeader);

            st.sendResponse(responseOK);
            Log.i("LogListener", name + ": Response = " + responseOK);
        } catch (Exception e) {
            Log.e("LogListener", name + " : Error : " + e);
        }
        try {
            ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
            if(!contentTypeHeader.equals(null)){
                Log.i("LogL","SCAIP");
                byte[] content = request.getRawContent();
                String body = new String(content, StandardCharsets.UTF_8);
                if(body.startsWith("<mrq>")) {
                    String ref1 = body.split("<ref>")[1];
                    String ref = ref1.split("</ref>")[0];
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("ref", ref);
                    String SCAIPString = global.getSCAIPConstruct().CreateResponse(map);
                    Request SCAIPResponse = createRequestEnd(SCAIPString, Request.MESSAGE);
                    CallID callIDHeader = (CallID) SCAIPResponse.getHeader(CallID.NAME);
                    String callID = callIDHeader.getCallId();
                    CSeq cSeqHeader = (CSeq) request.getHeader(CSeq.NAME);
                    Long cSeq = cSeqHeader.getSeqNumber();
                    Boolean bool = MDB.insertarRemensaje(callID, cSeq, ipAddressEnd, nameEnd, portEnd, Request.MESSAGE, SCAIPString);
                    Log.i("LogL", "Res" + SCAIPResponse);
                    sendRequest(SCAIPResponse);
                }
            }
        }catch (Exception e) {
            Log.e("LogListener", name + " : Error : " + e);
        }


    }


    @Override
    public void processResponse(ResponseEvent responseReceivedEvent) {

        Response response = (Response) responseReceivedEvent.getResponse();
        Log.i("LogListener", "pRes = " + response);

        try{
            //Necesitamos autenticarnos en el servidor
            if(response.getStatusCode() == Response.UNAUTHORIZED){
                // Obtenemos el encabezado de autenticación
                WWWAuthenticateHeader wwwAuthHeader = null;
                wwwAuthHeader= (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);
                if( wwwAuthHeader != null){
                    // Obtenemos los parámetros necesarios para el cálculo de la respuesta
                    String username = name;
                    String realm = wwwAuthHeader.getRealm();
                    String nonce = wwwAuthHeader.getNonce();
                    String password = "s1234"; // Reemplaza con tu contraseña

                    ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                    URI serverURI = toHeader.getAddress().getURI();

                    CSeq cSeqHeader = (CSeq) response.getHeader(CSeq.NAME);
                    Long cSeq = cSeqHeader.getSeqNumber();
                    String method = cSeqHeader.getMethod();


                    //Formula para el calculo de la respuesta de autenticacion
                    String a1 = username + ":" + realm + ":" + password;
                    String a2 = method + ":" + serverURI;
                    String responseValue = DigestUtils.md5Hex(DigestUtils.md5Hex(a1) + ":" + nonce + ":" + DigestUtils.md5Hex(a2));

                    AuthorizationHeader authHeader = headerFactory.createAuthorizationHeader("Digest");
                    authHeader.setUsername(username);
                    authHeader.setRealm(realm);
                    authHeader.setNonce(nonce);
                    authHeader.setURI(serverURI);
                    authHeader.setResponse(responseValue);
                    authHeader.setAlgorithm("MD5");

                    //Recuperamos el mensaje que ha sido denegado para reenviarlo
                    CallID callIDHeader = (CallID) response.getHeader(CallID.NAME);
                    String callID = callIDHeader.getCallId();
                    Remensaje remensaje = MDB.recuperarRemensajes("callID='" + callID + "'").get(0);
                    Request request = null;
                    if(remensaje.getIpAddress()!=null){
                        request = this.createRequest(remensaje.getIpAddress(), remensaje.getName(),remensaje.getPort(),remensaje.getMethod(),remensaje.getMessage());
                    }else {
                        request = this.createRequestEnd(remensaje.getMessage(), remensaje.getMethod());
                    }
                    // Agrega el encabezado de autorización a la solicitud
                    request.addHeader(authHeader);
                    request.setHeader(callIDHeader);
                    CSeqHeader newCSeqHeader = headerFactory.createCSeqHeader(cSeq +1L, remensaje.getMethod());
                    request.setHeader(newCSeqHeader);
                    Log.i("LogListener", "CSeq :"+ request );
                    sendRequest(request);
                }
            }
        }
        catch (Exception e){
            Log.i("sd","sds");
            Log.e("LogListener", String.valueOf(e));
        }
    }

    //El mensaje para mostrar que estamos vivos
    public void processOptions(RequestEvent requestEvent, ServerTransaction serverTransaction){
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            ServerTransaction st = requestEvent.getServerTransaction();
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }

            Response responseOK = messageFactory.createResponse(Response.OK, request);

            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transportProtocol).getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(name);
            contactHeader = headerFactory.createContactHeader(contactAddress);
            responseOK.addHeader(contactHeader);
            st.sendResponse(responseOK);
            Log.i("LogListener", "Response = " + responseOK);
        } catch (Exception e) {
            Log.e("LogListener","Error : " + e);
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
    }

    //No me importa nada de abajo
    public void processIOException(IOExceptionEvent exceptionEvent) {
        Log.e("LogListener","IOException happened for "
                + exceptionEvent.getHost() + " port = "
                + exceptionEvent.getPort());
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        Log.e("LogListener","Transaction terminated event recieved");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        Log.e("LogListener","Dialog Terminated Event");
    }



}
