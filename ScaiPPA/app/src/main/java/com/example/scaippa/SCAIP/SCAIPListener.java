package com.example.scaippa.SCAIP;

import android.gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import android.javax.sip.ClientTransaction;
import android.javax.sip.Dialog;
import android.javax.sip.DialogState;
import android.javax.sip.DialogTerminatedEvent;
import android.javax.sip.IOExceptionEvent;
import android.javax.sip.ListeningPoint;
import android.javax.sip.PeerUnavailableException;
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
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.Header;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.MaxForwardsHeader;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;
import android.util.Log;

import com.example.scaippa.basedatos.MiBaseDatos;

import java.util.ArrayList;
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

    private String name;
    private String ipAddress;
    private String port;
    private String ipAddressProxy;
    private String portProxy;
    private String transportProtocol;
    private String proxyMode;

    public String getListenerName() {return name;}
    public String getListenerIpAddress() {return ipAddress;}
    public String getListenerPort() {return port;}
    public String getListenerIpAddressProxy() {return ipAddressProxy;}
    public String getListenerPortProxy() {return portProxy;}
    public String getListenerTransportProtocol() {return transportProtocol;}
    public String getListenerProxyMode() {return proxyMode;}

    private MiBaseDatos MDB;

    public void MDBinsert(MiBaseDatos MDB){
        this.MDB=MDB;
    }

    //Inicializamos el Listener (Revisar protocolos transporte)
    public void init(String name, String ipAddress, String port, String ipAddressProxy, String portProxy, String transportProtocol, String proxyMode){
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.ipAddressProxy = ipAddressProxy;
        this.portProxy = portProxy;
        this.transportProtocol = transportProtocol;
        this.proxyMode = proxyMode;

        Log.i("LogListener", name + ": Creando Listener");

        //Inicialización de la factoria
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();

        //Este path determina donde estan las implementaciones de la libreria
        sipFactory.setPathName("android.gov.nist");

        //Declaracion de las propiedades
        Properties properties = new Properties();

        //Declaramos el router para los mensajes sip
        if(proxyMode.equals("ON")) {
            properties.setProperty("android.javax.sip.OUTBOUND_PROXY", ipAddressProxy + ":" + portProxy + "/" + transportProtocol);
        }
        properties.setProperty("android.javax.sip.STACK_NAME", name);
        //properties.setProperty("android.javax.sip.IP_ADDRESS", ip_address);

        //Según esto es para quitar la conexion del cliente tras la transaccion
        properties.setProperty("android.gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");


        try {
            // Crea la pila Sip con las propiedades que hemos puesto
            sipStack = sipFactory.createSipStack(properties);
            Log.i("LogListener", name + ": sipStack = " + sipStack);
        } catch (PeerUnavailableException e) {
            Log.e("LogListener", name + ": excepcion: " + e);
        }

        try {
            //Inicializamos las factorias necesarias
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();

            //Creamos un listening poing
            listeningPoint = sipStack.createListeningPoint(ipAddress, Integer.parseInt(port), transportProtocol);
            Log.i("LogListener", name + ": listeningPoint = " + listeningPoint);

            //Creamos un sip Provider
            sipProvider = sipStack.createSipProvider(listeningPoint);
            Log.i("LogListener", name + ": sipProvider = " + sipProvider);
            sipProvider.addSipListener(this);


        } catch (Exception e) {
            Log.e("LogListener", name + ": excepcion: " + e);

        }
    }

    public Request createRequest(String toSipAddress, String toUser, String toSipPort, String requestMethod) {
        Request request = null;

        try {
            // Crea cabecera From
            SipURI fromAddress = addressFactory.createSipURI(name, ipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(name);
            FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345");

            // Crea cabecera To
            SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toUser);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

            // Crea RequestURI
            SipURI requestURI = addressFactory.createSipURI(toUser, toSipAddress + ":" + toSipPort);

            // Crea ViaHeaders
            ArrayList viaHeaders = new ArrayList();
            String ipAddress = listeningPoint.getIPAddress();
            String transport = listeningPoint.getTransport();
            ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress, sipProvider.getListeningPoint(transport).getPort(), transport, null);
            viaHeaders.add(viaHeader);

            // Mas Headers
            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, requestMethod);

            // Crea Request
            request = messageFactory.createRequest(requestURI, requestMethod, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

            // Crea cabecera contacto
            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());
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
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain"); //puedo poner lo que me de la gana??
            request.setContent(dataMessage, contentTypeHeader);
        }catch(Exception e){
            Log.e("LogListener", name + ": Exception = " + e);
        }
        return request;
    }

    public void sendRequest(Request request){

        try {

            Header header = request.getHeader("Call-ID");
            String callID = header.toString().substring(9,41);
            MDB.insertarTransaccion(callID,0,request.toString());
            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);

            // send the request out.

            inviteTid.sendRequest();
            Log.i("LogListener", name + ": inviteTID = " + inviteTid);

            dialog = inviteTid.getDialog();
        }catch (Exception e){
            Log.e("LogListener", name + ": Exception = " + e);
        }



    }

    @Override
    public void processRequest(RequestEvent requestEvent) {

        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
        Log.i("LogListener", name + ": pReq = " + serverTransactionId);
        Log.i("LogListener", name + ": Request " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);
        Log.i("LogListener", name + ": Request " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + request);

        Header header = request.getHeader("Call-ID");
        String callID = header.toString().substring(9,41);
        MDB.insertarTransaccion(callID,0,request.toString());

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
        }

    }

    public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            Log.i("LogListener", name + " : Procesando INVITE");
            Response responseTrying = messageFactory.createResponse(Response.TRYING, request);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            //Seguramente haga falta un thread
            Header header = responseTrying.getHeader("Call-ID");
            String callID = header.toString().substring(9,41);
            MDB.insertarTransaccion(callID,0,responseTrying.toString());
            st.sendResponse(responseTrying);
            Log.i("LogListener", name + " : TRYING enviado");
            Log.i("LogListener", name + ": Response = " + responseTrying);

            Response responseOK = messageFactory.createResponse(Response.OK, request);

            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transportProtocol).getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(name);
            contactHeader = headerFactory.createContactHeader(contactAddress);

            responseOK.addHeader(contactHeader);


            header = responseOK.getHeader("Call-ID");
            callID = header.toString().substring(9,41);
            MDB.insertarTransaccion(callID,0,responseOK.toString());

            st.sendResponse(responseOK);
            Log.i("LogListener", name + ": Response = " + responseOK);
        } catch (Exception e) {
            Log.e("LogListener", name + " : Error : " + e);
        }
    }

    public void processAck(RequestEvent requestEvent,
                           ServerTransaction serverTransaction){
        Log.i("LogListener", name + ": RequestACK = " + requestEvent);

    }

    public void processBye(RequestEvent requestEvent,
                           ServerTransaction serverTransaction){

    }

    public void processCancel(RequestEvent requestEvent,
                           ServerTransaction serverTransaction){

    }

    public void processMessage(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            Log.i("LogListener", name + " : Procesando Message");
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            Response responseOK = messageFactory.createResponse(Response.OK, request);

            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transportProtocol).getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(name);
            contactHeader = headerFactory.createContactHeader(contactAddress);

            responseOK.addHeader(contactHeader);

            Header header = responseOK.getHeader("Call-ID");
            String callID = header.toString().substring(9,41);
            MDB.insertarTransaccion(callID,0,responseOK.toString());
            st.sendResponse(responseOK);
            Log.i("LogListener", name + ": Response = " + responseOK);
        } catch (Exception e) {
            Log.e("LogListener", name + " : Error : " + e);
        }

    }


    @Override
    public void processResponse(ResponseEvent responseReceivedEvent) {

        Log.i("LogListener", name + ": Response Recibida");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction tid = responseReceivedEvent.getClientTransaction();
        Log.i("LogListener", name + ": pRes = " + response);
        Log.i("LogListener", name + ": pRes = " + tid);
        Header header = response.getHeader("Call-ID");
        String callID = header.toString().substring(9,41);
        MDB.insertarTransaccion(callID,0,response.toString());
        CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        Dialog dialog = responseReceivedEvent.getDialog();
        try {
            Request requestACK = dialog.createAck(cSeqHeader.getSeqNumber());
            header = requestACK.getHeader("Call-ID");
            callID = header.toString().substring(9,41);
            MDB.insertarTransaccion(callID,0,requestACK.toString());
            dialog.sendAck(requestACK);
        }catch (Exception e){

        }

    }

    public void processOptions(RequestEvent requestEvent,
                              ServerTransaction serverTransaction){
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            Log.i("LogListener", name + " : Procesando Message");
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            Response responseOK = messageFactory.createResponse(Response.OK, request);

            SipURI contactURI = addressFactory.createSipURI(name, ipAddress);
            contactURI.setPort(sipProvider.getListeningPoint(transportProtocol).getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(name);
            contactHeader = headerFactory.createContactHeader(contactAddress);

            responseOK.addHeader(contactHeader);

            Header header = responseOK.getHeader("Call-ID");
            String callID = header.toString().substring(9,41);
            MDB.insertarTransaccion(callID,0,responseOK.toString());
            st.sendResponse(responseOK);
            Log.i("LogListener", name + ": Response = " + responseOK);
        } catch (Exception e) {
            Log.e("LogListener", name + " : Error : " + e);
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {


    }


    //No me importa nada de abajo
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException happened for "
                + exceptionEvent.getHost() + " port = "
                + exceptionEvent.getPort());
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction terminated event recieved");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("dialogTerminatedEvent");

    }
}