package Cmds;

/*
	created by kai47x in november 2017 
	(c) by zellview.org 
*/


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter; 
import java.io.FileWriter; 

public class SyncDirs {

	private static final String sep= File.separator;
	private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy kk:mm");     // 31.01.2016 20:07
	
	private String startRoot, destRoot;
	private PrintWriter wr;
	private LocalDateTime now;
	
	int filesTested, dirsTested, filesCopied, dirsCopied, filesDel, dirsDel;

	private void ResetCounter()
	{
		filesTested = 0; dirsTested = 0;
		filesCopied = 0; dirsCopied = 0;
		filesDel = 0; dirsDel = 0;
	}
	
	private void InitLog() {
	//BEGIN
		try 
		{
			wr = new PrintWriter(new BufferedWriter(new FileWriter("Log.txt")));			
		}
		catch(IOException e) {
			System.out.println( "Fehler beim öffnen der Logdatei ! " + e.getMessage() );
			System.exit(0);
		}
		
		wr.println("ZellviewJ Datensynchronisation");
		wr.println("\tQuellverzeichnis:\t" + this.startRoot);
		wr.println("\tZielverzeichnis:\t" + this.destRoot);
		wr.println("");		
        now = LocalDateTime.now();		
		wr.println("\tStart um:\t" + now.format(df));
		wr.println("");		
	//END InitLog;
	}
	
	private void CloseLog() {
	// BEGIN
        now = LocalDateTime.now();		
		wr.println("**********************************");
		wr.println("");        
		wr.println("\tBeendet um:\t" + now.format(df));
		wr.println("");		
		wr.println("Dateien geprüft " + filesTested);		
		wr.println("Dateien kopiert " + filesCopied);		
		wr.println("Dateien entfernt " + filesDel);		
		wr.println("");		
		wr.println("Verzeichnisse geprüft " + dirsTested);		
		wr.println("Verzeichnisse kopiert " + dirsCopied);		
		wr.println("Verzeichnisse entfernt " + dirsDel);		
		wr.flush();
		wr.close();
	// END CloseLog;
	}
	
	/* Verzeichnisse vergleichen ggf. kopieren */
	public void Do(String src, String dest)
	{
	// VAR
		File f, srcDir, destDir, destFile;
		File[] fList;
		String srcFileStr, destFileStr;

	//BEGIN
		srcDir = new File(src);
		fList = srcDir.listFiles();

		wr.println("");        	
		wr.println("**********************************");
		wr.println("Verzeichnis " + src );
		wr.println("mit " + dest + " abgleichen.");
		wr.println("");
		
		dirsTested++;

		/* existiert Zielverzeichnis, ggf erstellen */
        destDir = new File(dest);
        if ( ! destDir.exists()) {
    		wr.println("Erzeuge Zielverzeichnis " + destDir.getAbsolutePath());	            	
    		wr.println("");        	
        	destDir.mkdirs();
        	dirsCopied++;
        }
		
		for (File file: fList) 
		{
	        if (file.isFile()) {
	        	filesTested++;
	        	srcFileStr = file.getAbsolutePath();	            
	        	wr.println("\tQuelldatei " + srcFileStr);
	           
	        	destFileStr = dest + sep + file.getName();	            
	        	destFile = new File( destFileStr );
	            
	        	// Wenn Ziel nicht existiert oder Timestamp verschieden -> kopieren 
	        	if ( ( ! destFile.exists() ) || (destFile.lastModified() != file.lastModified() ) ) {
	        		filesCopied++;
	        	   	wr.println("\t\tkopieren nach " + destFileStr);
            		try {
            			System.out.println("copy");
            			Files.copy( Paths.get(srcFileStr), Paths.get(destFileStr) );
            		}
            		catch (IOException e) {
            			System.out.println(e.getMessage());
        	    		wr.println("\tFehler beim kopieren nach " + srcFileStr);
        	    		wr.println(e.getMessage());
            		} 
	            }
	        }
	        else // file is directory 
	        {
	        	// rekursiv nach subDirs
	            Do(file.getAbsolutePath(), dest + sep + file.getName());
	        }
		}
	//END Do;		 
	} 
	
	@SuppressWarnings("unused")
	private void DoReverse(String src, String dest)
	{
	// VAR
		File srcDir, destDir, srcFile;
		File[] fList;
		String srcFileStr, destFileStr;
	// BEGIN
		destDir = new File(dest);
		fList = destDir.listFiles();
		
		this.wr.println("");
		this.wr.println("**********************************");
		this.wr.println("Zielverzeichnis " + dest);
		this.wr.println("reverse abgleichen mit " + src);
		this.wr.println("");

		System.out.println("*****************");
		System.out.println("DoReverse Verzeichnis " + src);
		System.out.println("nach " + dest);
		System.out.println("*****************");
		
		for (File file : fList) 
		{
			if (file.isFile()) {
				destFileStr = file.getAbsolutePath();
				this.wr.println("\tZieldatei " + destFileStr);
	            
	            srcFileStr = src + sep + file.getName();
	            srcFile = new File( srcFileStr );
	            if ( ! srcFile.exists() ) {
	            	// Datei in Quelle nicht vorhanden
	            	filesDel++;
					this.wr.println("\t\tentfernt.");
	            	file.delete();
	            }
			}
			else
			{
				DoReverse(   src + sep + file.getName(),  dest + sep + file.getName() );
			}
		}
		srcDir =  new File(src);
		if ( ! srcDir.exists() )
		{
			// Zielverzeichnis in Quelle nicht vorhanden
			dirsDel++;
			this.wr.println("");
			this.wr.println("\tZielverzeichnis " + dest);
			this.wr.println("\t\tentfernt." + dest);
			this.wr.println("");
			destDir.delete();
		}
	} //END DoReverse;
	
	public void Start(String start, String dest) {
	// BEGIN		
		this.startRoot = start;
		this.destRoot = dest;
		System.out.println("start " + startRoot);
		System.out.println("dest " + destRoot);
		
		this.ResetCounter();
		this.InitLog();
		
		this.Do(start, dest);
		this.DoReverse(start, dest);
		
		this.CloseLog();
	// END Start;
	} 
}
