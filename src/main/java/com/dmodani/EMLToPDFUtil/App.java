package com.dmodani.EMLToPDFUtil;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) {
		File inputFile = new File("EMLfile.eml");
		File outputFile = new File("test.pdf");
		EMLUtils.writeToFile(inputFile, outputFile);
		
	}
}
