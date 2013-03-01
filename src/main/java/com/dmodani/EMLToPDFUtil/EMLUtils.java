package com.dmodani.EMLToPDFUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.Field;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author dmodani
 * 
 */
public class EMLUtils {

	public static void writeToFile(File inputFile, File outputFile) {
		System.out.println("EMLUtils.writeToFile()");
		MessageBuilder builder = new DefaultMessageBuilder();
		try {
			Message message = builder.parseMessage(FileUtils
					.openInputStream(inputFile));
			System.out.println("after openInputStream");
			Body body = message.getBody();
			if (body instanceof TextBody) {
				TextBody textBody = (TextBody) body;
				EMLUtils.textToPDF(EMLUtils.getString(textBody), outputFile);
			}
			if (body instanceof Multipart) {
				Multipart multipart = (Multipart) body;
				for (Entity part : multipart.getBodyParts()) {
					if (part.getMimeType().equals("text/html")) {
						TextBody textBody = (TextBody) part.getBody();						
						EMLUtils.xhtmlToPDF(textBody.getInputStream(), outputFile);
					}
				}
			}
		} catch (MimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void textToPDF(String textBody, File outputFile) {
		System.out.println("EMLUtils.textToPDF()");
		Document document  = new Document();
		try {
			PdfWriter.getInstance(document,  FileUtils.openOutputStream(outputFile));
			document.open();
			document.add(new Paragraph(textBody));
			document.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		 
	}

	private static void xhtmlToPDF(InputStream inputStream, File outputFile) {
		System.out.println("EMLUtils.xhtmlToPDF()");
		CleanerProperties cleanerProps = new CleanerProperties();
		cleanerProps.setTranslateSpecialEntities(true);
		cleanerProps.setTransResCharsToNCR(true);
		cleanerProps.setOmitComments(true);
		try {
			TagNode tagNode = new HtmlCleaner(cleanerProps).clean(inputStream);
			new PrettyXmlSerializer(cleanerProps).writeToFile(tagNode,
					"page.xhtml", "utf-8");
			String url2 = new File("page.xhtml").toURI().toURL().toString();
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(url2);
			renderer.layout();
			FileOutputStream outputStream = FileUtils.openOutputStream(outputFile);
			renderer.createPDF(outputStream);
			IOUtils.closeQuietly(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private static String getString(TextBody body) {
		StringBuilder sb = new StringBuilder();
		try {
			Reader r = body.getReader();
			int c;
			while ((c = r.read()) != -1) {
				sb.append((char) c);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	private void print(Header header) {
		for (Field field : header.getFields()) {
			if (field.getName().equalsIgnoreCase("from")
					|| field.getName().equalsIgnoreCase("to")
					|| field.getName().equalsIgnoreCase("subject"))
				System.out.println(field.getName() + ":" + field.getBody());
		}
	}
}
