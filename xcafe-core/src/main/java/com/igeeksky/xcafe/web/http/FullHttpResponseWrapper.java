package com.igeeksky.xcafe.web.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class FullHttpResponseWrapper implements HttpServletResponse {
	
	private FullHttpResponse response;
	
	private final ChannelHandlerContext ctx;
	
	public FullHttpResponseWrapper(ChannelHandlerContext ctx, FullHttpResponse response) {
		this.response = response;
		this.ctx = ctx;
	}

	public FullHttpResponse getResponse() {
		ByteBuf buf = response.content();
		response = (DefaultFullHttpResponse) response.replace(buf.writeBytes(content.toByteArray()));
		this.contentLength = response.content().readableBytes();
		
		HttpHeaders headers = response.headers();
		headers.add("Content-Length", this.contentLength);
		return response;
	}

    public HttpHeaders trailingHeaders() {
        return response.trailingHeaders();
    }

    public ByteBuf content() {
        return response.content();
    }

    public int refCnt() {
        return response.content().refCnt();
    }

    public FullHttpResponse retain() {
    	response.content().retain();
        return response;
    }

    public FullHttpResponse retain(int increment) {
    	response.content().retain(increment);
        return response;
    }

    public FullHttpResponse touch() {
    	response.content().touch();
        return response;
    }

    public FullHttpResponse touch(Object hint) {
    	response.content().touch(hint);
        return response;
    }

    public boolean release() {
        return response.content().release();
    }

    public boolean release(int decrement) {
        return response.content().release(decrement);
    }

    public FullHttpResponse setProtocolVersion(HttpVersion version) {
    	response.setProtocolVersion(version);
        return response;
    }

    public FullHttpResponse setStatus(HttpResponseStatus status) {
    	response.setStatus(status);
        return response;
    }

    public FullHttpResponse copy() {
        return replace(content().copy());
    }

    public FullHttpResponse duplicate() {
        return replace(content().duplicate());
    }

    public FullHttpResponse retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    public FullHttpResponse replace(ByteBuf content) {
        return new DefaultFullHttpResponse(response.protocolVersion(), response.status(), content, response.headers(), trailingHeaders());
    }

    @Override
    public int hashCode() {
        return response.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultFullHttpResponse)) {
            return false;
        }

        DefaultFullHttpResponse other = (DefaultFullHttpResponse) o;

        return super.equals(other) &&
               content().equals(other.content()) &&
               trailingHeaders().equals(other.trailingHeaders());
    }

    @Override
    public String toString() {
    	return response.toString();
    }
	

	private static final String CHARSET_PREFIX = "charset=";

	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	private static final String CONTENT_LENGTH_HEADER = "Content-Length";

	private static final String LOCATION_HEADER = "Location";

	private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");


	//---------------------------------------------------------------------
	// ServletResponse properties
	//---------------------------------------------------------------------

	private boolean outputStreamAccessAllowed = true;

	private boolean writerAccessAllowed = true;

	private String characterEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

	private boolean charset = false;

	private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

	private final ServletOutputStream outputStream = new ResponseServletOutputStream(this.content);

	private PrintWriter writer;

	private long contentLength = 0;

	private String contentType;

	private int bufferSize = 4096;

	private boolean committed;

	private Locale locale = Locale.getDefault();


	//---------------------------------------------------------------------
	// HttpServletResponse properties
	//---------------------------------------------------------------------

	private final List<Cookie> cookies = new ArrayList<Cookie>();

	private int status = HttpServletResponse.SC_OK;

	private String errorMessage;

	private String forwardedUrl;

	private final List<String> includedUrls = new ArrayList<String>();


	//---------------------------------------------------------------------
	// ServletResponse interface
	//---------------------------------------------------------------------

	/**
	 * Set whether {@link #getOutputStream()} access is allowed.
	 * <p>Default is {@code true}.
	 */
	public void setOutputStreamAccessAllowed(boolean outputStreamAccessAllowed) {
		this.outputStreamAccessAllowed = outputStreamAccessAllowed;
	}

	/**
	 * Return whether {@link #getOutputStream()} access is allowed.
	 */
	public boolean isOutputStreamAccessAllowed() {
		return this.outputStreamAccessAllowed;
	}

	/**
	 * Set whether {@link #getWriter()} access is allowed.
	 * <p>Default is {@code true}.
	 */
	public void setWriterAccessAllowed(boolean writerAccessAllowed) {
		this.writerAccessAllowed = writerAccessAllowed;
	}

	/**
	 * Return whether {@link #getOutputStream()} access is allowed.
	 */
	public boolean isWriterAccessAllowed() {
		return this.writerAccessAllowed;
	}

	/**
	 * Return whether the character encoding has been set.
	 * <p>If {@code false}, {@link #getCharacterEncoding()} will return a default encoding value.
	 */
	public boolean isCharset() {
		return this.charset;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
		this.charset = true;
		updateContentTypeHeader();
	}

	private void updateContentTypeHeader() {
		if (this.contentType != null) {
			StringBuilder sb = new StringBuilder(this.contentType);
			if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) && this.charset) {
				sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
			}
			doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
		}
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public ServletOutputStream getOutputStream() {
		if (!this.outputStreamAccessAllowed) {
			throw new IllegalStateException("OutputStream access not allowed");
		}
		return this.outputStream;
	}

	@Override
	public PrintWriter getWriter() throws UnsupportedEncodingException {
		if (!this.writerAccessAllowed) {
			throw new IllegalStateException("Writer access not allowed");
		}
		if (this.writer == null) {
			byte[] bytes = response.content().array();
			this.content.write(bytes, 0, bytes.length);
			Writer targetWriter = (this.characterEncoding != null ?
					new OutputStreamWriter(this.content, this.characterEncoding) : new OutputStreamWriter(this.content));
			this.writer = new ResponsePrintWriter(targetWriter);
		}
		return this.writer;
	}

	public byte[] getContentAsByteArray() {
		flushBuffer();
		return response.content().array();
	}

	public String getContentAsString() throws UnsupportedEncodingException {
		flushBuffer();
		return (this.characterEncoding != null ? 
				response.content().toString(Charset.forName(this.characterEncoding)) : response.content().toString());
	}

	@Override
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
		doAddHeaderValue(CONTENT_LENGTH_HEADER, contentLength, true);
	}

	public int getContentLength() {
		this.contentLength = response.content().readableBytes();
		return (int) this.contentLength;
	}

	public void setContentLengthLong(long contentLength) {
		this.contentLength = contentLength;
		doAddHeaderValue(CONTENT_LENGTH_HEADER, contentLength, true);
	}

	public long getContentLengthLong() {
		this.contentLength = response.content().readableBytes();
		return this.contentLength;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
		if (contentType != null) {
			try {
				MediaType mediaType = MediaType.parseMediaType(contentType);
				if (mediaType.getCharset() != null) {
					this.characterEncoding = mediaType.getCharset().name();
					this.charset = true;
				}
			}
			catch (Exception ex) {
				// Try to get charset value anyway
				int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
				if (charsetIndex != -1) {
					this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
					this.charset = true;
				}
			}
			updateContentTypeHeader();
		}
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public int getBufferSize() {
		return this.bufferSize;
	}

	@Override
	public void flushBuffer() {
		setCommitted(true);
	}

	@Override
	public void resetBuffer() {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot reset buffer - response is already committed");
		}
		response.content().clear();
	}

	private void setCommittedIfBufferSizeExceeded() {
		int bufSize = getBufferSize();
		if (bufSize > 0 && response.content().readableBytes() > bufSize) {
			setCommitted(true);
		}
	}

	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	@Override
	public boolean isCommitted() {
		return this.committed;
	}

	@Override
	public void reset() {
		resetBuffer();
		this.characterEncoding = null;
		this.contentLength = 0;
		this.contentType = null;
		this.locale = null;
		this.cookies.clear();
		this.response.headers().clear();
		this.status = HttpServletResponse.SC_OK;
		this.errorMessage = null;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public void addCookie(Cookie cookie) {
		Assert.notNull(cookie, "Cookie must not be null");
		this.cookies.add(cookie);
	}

	public Cookie[] getCookies() {
		return this.cookies.toArray(new Cookie[this.cookies.size()]);
	}

	public Cookie getCookie(String name) {
		Assert.notNull(name, "Cookie name must not be null");
		for (Cookie cookie : this.cookies) {
			if (name.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	@Override
	public boolean containsHeader(String name) {
		return response.headers().contains(name);
	}

	/**
	 * Return the names of all specified headers as a Set of Strings.
	 * <p>As of Servlet 3.0, this method is also defined HttpServletResponse.
	 * @return the {@code Set} of header name {@code Strings}, or an empty {@code Set} if none
	 */
	@Override
	public Collection<String> getHeaderNames() {
		return response.headers().names();
	}

	/**
	 * Return the primary value for the given header as a String, if any.
	 * Will return the first value in case of multiple values.
	 * <p>As of Servlet 3.0, this method is also defined in HttpServletResponse.
	 * As of Spring 3.1, it returns a stringified value for Servlet 3.0 compatibility.
	 * Consider using {@link #getHeaderValue(String)} for raw Object access.
	 * @param name the name of the header
	 * @return the associated header value, or {@code null} if none
	 */
	@Override
	public String getHeader(String name) {
		return (response.headers() != null ? response.headers().get(name) : null);
	}

	/**
	 * Return all values for the given header as a List of Strings.
	 * <p>As of Servlet 3.0, this method is also defined in HttpServletResponse.
	 * As of Spring 3.1, it returns a List of stringified values for Servlet 3.0 compatibility.
	 * Consider using {@link #getHeaderValues(String)} for raw Object access.
	 * @param name the name of the header
	 * @return the associated header values, or an empty List if none
	 */
	@Override
	public List<String> getHeaders(String name) {
		HttpHeaders headers = response.headers();
		if(null != headers){
			return headers.getAll(name) != null ? headers.getAll(name) : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	/**
	 * Return the primary value for the given header, if any.
	 * <p>Will return the first value in case of multiple values.
	 * @param name the name of the header
	 * @return the associated header value, or {@code null} if none
	 */
	public Object getHeaderValue(String name) {
		HttpHeaders headers = response.headers();
		if(null != headers){
			return headers.get(name) != null ? headers.get(name) : null;
		}
		return null;
	}

	/**
	 * The default implementation returns the given URL String as-is.
	 * <p>Can be overridden in subclasses, appending a session id or the like.
	 */
	@Override
	public String encodeURL(String url) {
		return url;
	}

	/**
	 * The default implementation delegates to {@link #encodeURL},
	 * returning the given URL String as-is.
	 * <p>Can be overridden in subclasses, appending a session id or the like
	 * in a redirect-specific fashion. For general URL encoding rules,
	 * override the common {@link #encodeURL} method instead, applying
	 * to redirect URLs as well as to general URLs.
	 */
	@Override
	public String encodeRedirectURL(String url) {
		return encodeURL(url);
	}

	@Override
	@Deprecated
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	@Override
	@Deprecated
	public String encodeRedirectUrl(String url) {
		return encodeRedirectURL(url);
	}

	@Override
	public void sendError(int status, String errorMessage) throws IOException {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot set error status - response is already committed");
		}
		this.status = status;
		this.errorMessage = errorMessage;
		response.setStatus(HttpResponseStatus.valueOf(status));
		setCommitted(true);
	}

	@Override
	public void sendError(int status) throws IOException {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot set error status - response is already committed");
		}
		this.status = status;
		response.setStatus(HttpResponseStatus.valueOf(status));
		setCommitted(true);
	}

	@Override
	public void sendRedirect(String url) throws IOException {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot send redirect - response is already committed");
		}
		Assert.notNull(url, "Redirect URL must not be null");
		setHeader(LOCATION_HEADER, url);
		setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		setCommitted(true);
	}

	public String getRedirectedUrl() {
		return getHeader(LOCATION_HEADER);
	}

	@Override
	public void setDateHeader(String name, long value) {
		setHeaderValue(name, formatDate(value));
	}

	public long getDateHeader(String name) {
		try {
			HttpHeaders headers = response.headers();
			if(null != headers){
				Long times;
				return null != (times = headers.getTimeMillis(name)) ? times : -1l; 
			}
			return -1l;
		}catch(Exception e){
			throw new IllegalArgumentException("Value for header '" + name + "' is not a valid Date: " + getHeader(name));
		}
	}

	@Override
	public void addDateHeader(String name, long value) {
		addHeaderValue(name, formatDate(value));
	}

	private String formatDate(long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		dateFormat.setTimeZone(GMT);
		return dateFormat.format(new Date(date));
	}

	@Override
	public void setHeader(String name, String value) {
		setHeaderValue(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		addHeaderValue(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		setHeaderValue(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		addHeaderValue(name, value);
	}

	private void setHeaderValue(String name, Object value) {
		if (setSpecialHeader(name, value)) {
			return;
		}
		doAddHeaderValue(name, value, true);
	}

	private void addHeaderValue(String name, Object value) {
		if (setSpecialHeader(name, value)) {
			return;
		}
		doAddHeaderValue(name, value, false);
	}

	private boolean setSpecialHeader(String name, Object value) {
		if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
			setContentType(value.toString());
			return true;
		}
		else if (CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
			setContentLength(value instanceof Number ? ((Number) value).intValue() :
					Integer.parseInt(value.toString()));
			return true;
		}
		else {
			return false;
		}
	}

	private void doAddHeaderValue(String name, Object value, boolean replace) {
		HttpHeaders headers = response.headers();
		String header = headers.get(name);
		Assert.notNull(value, "Header value must not be null");
		if (header == null) {
			headers.add(name, value);
		}
		if (replace) {
			headers.set(name, value);
		} else {
			headers.add(name, value);
		}
	}

	@Override
	public void setStatus(int status) {
		if (!this.isCommitted()) {
			response.setStatus(HttpResponseStatus.valueOf(status));
			this.status = status;
		}
	}

	@Override
	@Deprecated
	public void setStatus(int status, String errorMessage) {
		if (!this.isCommitted()) {
			this.status = status;
			this.errorMessage = errorMessage;
			response.setStatus(HttpResponseStatus.valueOf(status));
		}
	}

	@Override
	public int getStatus() {
		this.status = response.status().code();
		return this.status;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}


	//---------------------------------------------------------------------
	// Methods for MockRequestDispatcher
	//---------------------------------------------------------------------

	public void setForwardedUrl(String forwardedUrl) {
		this.forwardedUrl = forwardedUrl;
	}

	public String getForwardedUrl() {
		return this.forwardedUrl;
	}

	public void setIncludedUrl(String includedUrl) {
		this.includedUrls.clear();
		if (includedUrl != null) {
			this.includedUrls.add(includedUrl);
		}
	}

	public String getIncludedUrl() {
		int count = this.includedUrls.size();
		if (count > 1) {
			throw new IllegalStateException(
					"More than 1 URL included - check getIncludedUrls instead: " + this.includedUrls);
		}
		return (count == 1 ? this.includedUrls.get(0) : null);
	}

	public void addIncludedUrl(String includedUrl) {
		Assert.notNull(includedUrl, "Included URL must not be null");
		this.includedUrls.add(includedUrl);
	}

	public List<String> getIncludedUrls() {
		return this.includedUrls;
	}


	/**
	 * Inner class that adapts the ServletOutputStream to mark the
	 * response as committed once the buffer size is exceeded.
	 */
	private class ResponseServletOutputStream extends XcafeServletOutputStream {

		public ResponseServletOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public void write(int b) throws IOException {
			/*ByteBuf buf = ctx.alloc().buffer(4);
			ctx.channel().writeAndFlush(buf.writeByte(b));*/
			super.write(b);
			super.flush();
			setCommittedIfBufferSizeExceeded();
		}

		@Override
		public void flush() throws IOException {
			//ctx.channel().flush();
			setCommitted(true);
		}
	}


	/**
	 * Inner class that adapts the PrintWriter to mark the
	 * response as committed once the buffer size is exceeded.
	 */
	private class ResponsePrintWriter extends PrintWriter {

		public ResponsePrintWriter(Writer out) {
			super(out, true);
		}

		@Override
		public void write(char buf[], int off, int len) {
			//ctx.channel().writeAndFlush(Unpooled.copiedBuffer(buf, off, len, Charset.forName("utf-8")));
			super.write(buf, off, len);
			super.flush();
			setCommittedIfBufferSizeExceeded();
		}

		@Override
		public void write(String s, int off, int len) {
			//ctx.channel().writeAndFlush(Unpooled.copiedBuffer(s, off, len, Charset.forName("utf-8")));
			
			super.write(s, off, len);
			super.flush();
			setCommittedIfBufferSizeExceeded();
		}

		@Override
		public void write(int c) {
			/*ByteBuf buf = ctx.alloc().buffer(4);
			ctx.channel().writeAndFlush(buf.writeByte(c));*/
			super.write(c);
			super.flush();
			setCommittedIfBufferSizeExceeded();
		}

		@Override
		public void flush() {
			ctx.channel().flush();
			super.flush();
			setCommitted(true);
		}
	}

}
