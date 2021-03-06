package org.boilit.bsl;

import org.boilit.bsl.core.ExecuteContext;
import org.boilit.bsl.core.IExecute;
import org.boilit.bsl.core.Parser;
import org.boilit.bsl.encoding.EncoderFactory;
import org.boilit.bsl.encoding.IEncoder;
import org.boilit.bsl.exception.ScriptException;
import org.boilit.bsl.formatter.FormatterManager;
import org.boilit.bsl.xio.BytesPrinter;
import org.boilit.bsl.xio.CharsPrinter;
import org.boilit.bsl.xio.IPrinter;
import org.boilit.bsl.xio.IResource;
import org.boilit.bsl.xtc.ITextCompressor;
import org.boilit.logger.ILogger;

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * @author Boilit
 * @see
 */
public final class Template {
    private final Engine engine;
    private final IResource resource;
    private final IExecute executor;
    private final FormatterManager formatterManager;

    protected Template(final Engine engine, final IResource resource, final FormatterManager formatterManager) {
        this.engine = engine;
        this.resource = resource;
        this.formatterManager = formatterManager;
        final Parser parser = new Parser();
        parser.setTemplate(this);
        Reader reader = null;
        try {
            this.executor = parser.parse(reader = resource.openReader());
        } catch (ScriptException e) {
            throw new RuntimeException(e.toScriptException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public final ILogger getLogger() {
        return engine.getLogger();
    }

    public final Engine getEngine() {
        return engine;
    }

    public final IResource getResource() {
        return resource;
    }

    public final String getInputEncoding() {
        return engine.getInputEncoding();
    }

    public final String getOutputEncoding() {
        return engine.getOutputEncoding();
    }

    public final boolean isSpecifiedEncoder() {
        return engine.isSpecifiedEncoder();
    }

    public final ITextCompressor getTextCompressor() {
        return engine.getTextCompressor();
    }

    public final FormatterManager getFormatterManager() {
        return formatterManager;
    }

    public final Object execute(final Map<String, Object> model, final OutputStream outputStream) {
        final IEncoder encoder = EncoderFactory.getEncoder(this.getOutputEncoding(), this.isSpecifiedEncoder());
        return this.execute(model, new BytesPrinter(outputStream, encoder));
    }

    public final Object execute(final Map<String, Object> model, final Writer writer) {
        final IEncoder encoder = EncoderFactory.getEncoder(this.getOutputEncoding(), this.isSpecifiedEncoder());
        return this.execute(model, new CharsPrinter(writer, encoder));
    }

    public final Object execute(final Map<String, Object> model, final IPrinter printer) {
        return this.execute(new ExecuteContext(model, printer));
    }

    public final Object execute(final ExecuteContext context) {
        Object value = null;
        try {
            value = executor.execute(context);
            context.getPrinter().flush();
            context.clear();
        } catch (ScriptException e) {
            ScriptException se = e.toScriptException();
            this.getLogger().error(se, "Template[{}] execute error!", this.getResource().getName());
            throw new RuntimeException(se);
        } catch (Exception e) {
            this.getLogger().error(e, "Template[{}] execute error!", this.getResource().getName());
            throw new RuntimeException(e);
        }
        return value;
    }
}
