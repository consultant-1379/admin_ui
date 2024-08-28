package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

public class VectorCounterBinDetails extends EtlguiServlet{

	  private final Log log = LogFactory.getLog(this.getClass());
	@Override
	public Template doHandleRequest(HttpServletRequest request,
			HttpServletResponse response, Context ctx) throws Exception {
		// TODO Auto-generated method stub
		Template vectorcounterbin = null;
		vectorcounterbin = getTemplate("vectorcounterbin.vm");
		ctx.put("vectorcounterbin", getVectorcounterbin());
		return vectorcounterbin;
	}
	private ArrayList<ArrayList> getVectorcounterbin() {
		// TODO Auto-generated method stub
		File vectorcounterbinfile = new File("//eniq//sw//installer//vectorcounterbinfile.csv");
		String line = null;
		ArrayList<ArrayList> vectorcounterbin = new ArrayList();
		try {
			log.info("Extracting Vector Counter Bin information from the file");
			FileReader fr = new FileReader(vectorcounterbinfile);
			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();
			while(line!=null)
			{
				ArrayList row = new ArrayList();
				String[] columns = line.split("\\|");
				String ViewName = columns[0];
				String CounterName = columns[1];
				Integer NumberOfBinsLoaded = Integer.parseInt(columns[2]);
				String ROP = columns[3];
				String SN = columns[4];
				
				row.add(ViewName);
				row.add(CounterName);
				row.add(NumberOfBinsLoaded);
				row.add(ROP);
				row.add(SN);
				vectorcounterbin.add(row);
				line = br.readLine();
			}
			Collections.sort(vectorcounterbin, new Comparator<ArrayList>() 
			{
				@Override
				public int compare(ArrayList one, ArrayList two) 
				{
					return ((Integer) two.get(2)).compareTo((Integer) one.get(2));
				}
			});
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("An Exception occurred : "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("An Exception occurred : "+e.getMessage());
			e.printStackTrace();
		}

		return vectorcounterbin;
	}

}
