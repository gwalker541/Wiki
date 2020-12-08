import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import edu.stanford.nlp.simple.Sentence;

//import edu.arizona.cs.ResultClass;

public class Wiki {
	//"/C:/Users/Graham/Desktop/CSC483WebSearchTextRetrieval/Walker_Graham_WikiProj/WikiPages/"
	String inDir = "WikiPages";
	Analyzer analyzer;
	Directory index;
	
	public Wiki() {
        this.analyzer = new StandardAnalyzer();
        this.index = new RAMDirectory();
	}

	public static void main(String[] args) throws IOException {
		Wiki wiki = new Wiki();
		//wiki.read(args[0]);
		wiki.read("/C:/Users/Graham/Desktop/CSC483WebSearchTextRetrieval/Walker_Graham_WikiProj/WikiPages/");
		System.out.println("Finished reading");
		wiki.getAnswer("THE RESIDENTS Don Knotts took over from Norman Fell as the resident landlord on this sitcom");

	}
	
	private void read(String dir) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        
        
		IndexWriter w = null;
		try {
			w = new IndexWriter(index, config);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		File folder = new File(dir);
		//System.out.println(dir);
		File[] listOfFiles = folder.listFiles();
		String title = "";
		String prevTitle = "";
		String text = "";
		boolean newTitle = false;
		for (File file : listOfFiles) {
	      BufferedReader br = null;
	      try {
	         br = new BufferedReader(new FileReader(new File(file.getAbsolutePath())));
	         String line;
	         while((line = br.readLine()) != null) {
	        	 if (line.length() > 2 && line.substring(0, 2).equals("[[")) {
	        		 if ((line.length() > 7 && line.substring(0, 7).equals("[[File:")) || line.length() > 8 && line.substring(0, 8).equals("[[Image:")) {
	        			 continue;
	        		 }
	        		 else {
	        			 prevTitle = title;
	        			 title = line.substring(2, line.length() - 2);
	        			 newTitle = true;

	        		 }
	        	 }
	        	 else {
	        		 newTitle = false;
	        		 //text = lemmatizeLine(line);
	        		 text += line;
	        	 }
	        	 if (newTitle && !(text.equals(""))) {
	        		 addDoc(w, text, prevTitle);
	        		// System.out.println(prevTitle);
	        		 text = "";
	        	 }
	                        
	         }
	         //System.out.println(title);
	        //System.out.println(text);
	         addDoc(w, text, title);
	      } catch (FileNotFoundException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } finally {
	         if (br != null) {
	            try {
	               br.close();
	            } catch (IOException e) {
	               e.printStackTrace();
	            }
	         }
	      }
		}
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String lemmatizeLine(String line) {
		line = line.replaceAll("\\W", " ");
		//System.out.println("Hi" + line);
		if(line.trim().isEmpty()) {
			return "";
		}
		
		String lemmatizedLine = "";
		Sentence sentence = new Sentence(line);
		List<String> lemmas = sentence.lemmas();
		for (String word : lemmas) {
			lemmatizedLine += word + " ";
		}
		
		return lemmatizedLine;
	}
	
    private static void addDoc(IndexWriter w, String docText, String docTitle) throws IOException {
    	Document doc = new Document();
    	
    	doc.add(new TextField("docText", docText, Field.Store.YES));
    	doc.add(new StringField("docid", docTitle, Field.Store.YES));
    	w.addDocument(doc);
    }
    
    public String getAnswer(String query) throws IOException {
    	Set set = StandardAnalyzer.STOP_WORDS_SET;
    	//System.out.println(set);
        Query q = null;
        try {
			q = new QueryParser("docText", analyzer).parse(query);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        int hitsPerPage = 10; 
        IndexReader reader = DirectoryReader.open(this.index);
        IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(d.get("docid"));
            
        }
        System.out.println("in getAnswer");
        return "";
    }
    


}
