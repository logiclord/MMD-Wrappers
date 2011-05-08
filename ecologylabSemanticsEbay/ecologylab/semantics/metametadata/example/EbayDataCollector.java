package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

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

public class EbayDataCollector implements DispatchTarget<DocumentClosure>
{
	private OutputStream	out;

	public EbayDataCollector(OutputStream out)
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
		String seedUrlStr = "http://cgi.ebay.com/Elegant-Soiree-Art-Print-Victor-Gabriel-Gilbert-/120692445347?pt=Art_Prints&hash=item1c19d490a3";
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
		if (doc != null && doc instanceof EbayReport)
		{
			EbayReport report = (EbayReport) doc;
			//if (report.getProductName() != null)
			{
				synchronized (out)
				{
					String s = String.format(
							"\nProduct Name\t: %s\nImage URL\t\t: %s\nProduct Price\t: %s\nCondition\t\t: %s\nQuantity\t\t: %s\nShipping cost\t: %s\nShipping Type\t: %s\nReturns\t\t\t: %s",
							report.getProductName(),
							report.getImage(),
							report.price(),
							report.getCondition(),
							report.getQuantityAvailable(),
							report.getShippingCost(),
							report.getShippingType(),
							report.getReturnDetails()
					);

					int counter=0;

					// other info

					List<OtherInfo> oth= report.getOtherInfoGenres();

					if(oth!=null)
					{
						int comSize= oth.size();
						s+="\nOther info :-\n";
						while(counter<comSize)
						{
							s+="\nItem Number   : "+ oth.get(counter).getItemNumber();
							s+="\nItem Location : "+ oth.get(counter).getItemLocation();
							s+="\nShips To      : "+ oth.get(counter).getShipsTo();
							counter++;
						}
					}		
					
					//sellor info
					
					counter=0;
					List<SellorInfo> sel= report.getSellorInfoGenres();

					if(sel!=null)
					{
						int comSize= sel.size();
						s+="\n\nSellor info :-\n";
						while(counter<comSize)
						{
							s+="\nName       : "+ sel.get(counter).getSellorName();
							s+="\nFeedback   : "+ sel.get(counter).getFeeback();
							s+="\nPositive   : "+ sel.get(counter).getPositive();
							counter++;
						}
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
		OutputStream out = new FileOutputStream(new File("output.csv"));
		out.write("Details are as follows :- \n".getBytes("utf-8"));
		EbayDataCollector nwdc = new EbayDataCollector(out);
		nwdc.collect();
	}

}
