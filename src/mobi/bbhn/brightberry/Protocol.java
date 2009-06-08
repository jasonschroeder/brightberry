package mobi.bbhn.brightberry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.FilterBaseInterface;

public final class Protocol implements FilterBaseInterface, HttpConnection {
    private HttpConnection _subConnection;

    public Connection openFilter( String name, int mode, boolean timeouts ) throws IOException {
    	System.out.println("In filter");
    	System.out.println("Filter called on: " + name);
    	System.out.println("Filter Mode: " + mode);
        _subConnection = (HttpConnection)Connector.open("http:" + name + ";usefilter=false", mode, timeouts);
        if (_subConnection != null) {
            return this;
        }
        return null;
    }

    public String getURL() {
        return _subConnection.getURL();
    }

    public String getProtocol() {
        return _subConnection.getProtocol();
    }

    public String getHost() {
        return _subConnection.getHost();
    }

    public String getFile() {
        return _subConnection.getFile();
    }

    public String getRef() {
        return _subConnection.getRef();
    }

    public String getQuery() {
        return _subConnection.getQuery();
    }

    public int getPort() {
        return _subConnection.getPort();
    }

    public String getRequestMethod() {
        return _subConnection.getRequestMethod();
    }

    public void setRequestMethod(String method) throws IOException {
        _subConnection.setRequestMethod(method);
    }

    public String getRequestProperty(String key) {
        return _subConnection.getRequestProperty(key);
    }

    public void setRequestProperty(String key, String value) throws IOException {
        System.out.println("Request property <key, value>: " + key + ", " + value );
        _subConnection.setRequestProperty(key, value);
    }

    public int getResponseCode() throws IOException {
        return _subConnection.getResponseCode();
    }
    
    public String getResponseMessage() throws IOException {
        return _subConnection.getResponseMessage();
    }

    public long getExpiration() throws IOException {
        return _subConnection.getExpiration();
    }

    public long getDate() throws IOException {
        return _subConnection.getDate();
    }

    public long getLastModified() throws IOException {
        return _subConnection.getLastModified();
    }

    public String getHeaderField(String name) throws IOException {
        String value = _subConnection.getHeaderField(name);
        System.out.println("Response property <key, value>: " + name + ", " + value );
        return value;
    }

    public int getHeaderFieldInt(String name, int def) throws IOException {
        return _subConnection.getHeaderFieldInt(name, def);
    }

    public long getHeaderFieldDate(String name, long def) throws IOException {
        return _subConnection.getHeaderFieldDate(name, def);
    }

    public String getHeaderField(int n) throws IOException {
        return _subConnection.getHeaderField(n);
    }
    
    public String getHeaderFieldKey(int n) throws IOException {
        return _subConnection.getHeaderFieldKey(n);
    }
 
    public String getType() {
        return _subConnection.getType();
    }

    public String getEncoding() {
        return _subConnection.getEncoding();
    }

    public long getLength() {
        return _subConnection.getLength();
    }

    public InputStream openInputStream() throws IOException {
        return _subConnection.openInputStream();
    }
    
    public DataInputStream openDataInputStream() throws IOException {
        return _subConnection.openDataInputStream();
    }

    public OutputStream openOutputStream() throws IOException {
        return _subConnection.openOutputStream();
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return _subConnection.openDataOutputStream();
    }

    public void close() throws IOException {
        _subConnection.close();
    }
    
}
