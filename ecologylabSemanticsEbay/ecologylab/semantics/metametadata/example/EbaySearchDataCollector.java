package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.w3c.tidy.Report;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.generated.library.ebay.*;
import ecologylab.semantics.generated.library.CommentsHub;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

public class EbaySearchDataCollector implements DispatchTarget<DocumentClosure>
{
	private OutputStream	out;

	public EbaySearchDataCollector(OutputStream out)
	{
		this.out = out;
	}

	public void collect()
	{
		// register our own semantic action
		SemanticAction.register(SaveImageSemanticAction.class);

		// create the infoCollector
		NewInfoCollector infoCollector = new NewInfoCollector(GeneratedMetadataTranslationScope.get());

		// seeding start url
		String seedUrlStr = "http://shop.ebay.com/i.html?_nkw=iphone&_sacat=0&_odkw=eveve&_osacat=0&_trksid=p3286.c0.m270.l1313";
		ParsedURL seedUrl = ParsedURL.getAbsolute(seedUrlStr);
		Document seedDoc = infoCollector.getOrConstructDocument(seedUrl);
		DocumentClosure downloadClosure = seedDoc.getOrConstructClosure();
		downloadClosure.setDispatchTarget(this);
		downloadClosure.queueDownload();

		// request the download monitor to stop (after all the downloads are handled),
		// so we can exit gracefully.
		DownloadMonitor downloadMonitor = downloadClosure.downloadMonitor();
		downloadMonitor.requestStop();
	}

	@Override
	public void delivery(DocumentClosure o)
	{
		Document doc = o.getDocument();
		if (doc != null && doc instanceof EbaySearchReport)
		{
			EbaySearchReport report = (EbaySearchReport) doc;
			//if (report.getProductName() != null)
			{
				synchronized (out)
				{
					String s="\n";
					
				List<EbaySearchNode> res= report.getGenres();
				
					int resSize= res.size();
					int counter=0;
				
			
					while(counter<resSize)
					{
						s+="\n\nProduct Name   : "+ res.get(counter).getProductName();
						s+="\nAvailable Mode : "+ res.get(counter).getModeAvailable();
						s+="\nPrice          : "+ res.get(counter).getPrice();
						s+="\nProduct Image  : "+ res.get(counter).getImage();
						counter++;
					}
					
					
					try
					{
						out.write(s.getBytes("utf-8"));
						out.flush();
					}
					catch (UnsupportedEncodingException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException
	{
		// prepare output file (it will be closed by the system after program stops)
		OutputStream out = new FileOutputStream(new File("output_search.csv"));
		out.write("Search results are as follows :- \n".getBytes("utf-8"));
		EbaySearchDataCollector nwdc = new EbaySearchDataCollector(out);
		nwdc.collect();
	}

}
