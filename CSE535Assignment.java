import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Bhavika
 *
 */
public class CSE535Assignment {

	private HashMap<String,LinkedList<Document>> taat_postings = new HashMap<String,LinkedList<Document>>();
	private HashMap<String,LinkedList<Document>> daat_postings = new HashMap<String,LinkedList<Document>>();
	private HashMap<String,Integer> document_frequency_map = new HashMap<String,Integer>();
	private PrintWriter out = null;

	class Document{

		private Integer documentId;
		private Integer termFrequency;

		public Integer getDocumentId() {
			return documentId;
		}

		public void setDocumentId(Integer documentId) {
			this.documentId = documentId;
		}

		public Integer getTermFrequency() {
			return termFrequency;
		}

		public void setTermFrequency(Integer termFrequency) {
			this.termFrequency = termFrequency;
		}
	}


	/**
	 * For sorting indexes based on decreasing term frequencies.
	 * @author Bhavika
	 */
	class TermFrequencySortComparator implements Comparator<Document>{

		@Override
		public int compare(Document o1, Document o2){
			return o2.getTermFrequency().compareTo(o1.getTermFrequency());
		}
	}
	
	 
	/**
	 * @author Bhavika
	 * For sorting the postings list based on size.
	 */
	class PostingListSizeComparator implements Comparator<List>{
		@Override
		public int compare(List o1, List o2) {
			return o1.size() - o2.size();
		}

	}


	/**
	 * @author Bhavika
	 * For sorting indexes based on increasing documentId
	 */
	class DocumentIdSortComparator implements Comparator<Document>{

		@Override
		public int compare(Document o1, Document o2) {
			return o1.getDocumentId().compareTo(o2.getDocumentId());
		}
	}


	public static void main(String[] args) throws ClassNotFoundException, IOException {

		Class cls = Class.forName("CSE535Assignment");
		ClassLoader cLoader = cls.getClassLoader();
		InputStream inputStream = cLoader.getResourceAsStream(args[0]);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		CSE535Assignment obj = new CSE535Assignment();

		String line = null;

		try {			

			while((line = bufferedReader.readLine()) != null) {

				String[] input = line.split("\\\\");
				obj.initializePostingsMap(input);

			}

			obj.out = new PrintWriter(new FileWriter(args[1]), true);
			obj.getTopKTerms(Integer.parseInt(args[2]));

			/** Read the query_terms.txt file ***/

			inputStream = cLoader.getResourceAsStream(args[3]);
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);

			while((line =  bufferedReader.readLine()) != null){

				String[] terms = line.split(" ");
				obj.getPostings(terms);
				obj.docAtATimeQueryAnd(terms);
				obj.docAtATimeQueryOr(terms);
				/*** calling these methods twice
				 * 
				 * flag : true indicates optimization
				 * flag : false without optimization
				 * 
				 */
				obj.termAtATimeQueryOr(terms,false);
				obj.termAtATimeQueryOr(terms,true);
				obj.termAtATimeQueryAnd(terms,false);
				obj.termAtATimeQueryAnd(terms,true);
				
			}

		} catch (IOException e) {
			System.out.println("Error while reading term.idx file");
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();

		}finally{
			bufferedReader.close();
			obj.out.close();

		}
	}


	private void initializePostingsMap(String[] inputArr){

		String term = inputArr[0]; 
		String postingListSize = inputArr[1];
		String postings = inputArr[2];

		//set the document frequency map.
		document_frequency_map.put(term, Integer.valueOf(postingListSize.substring(1, postingListSize.length())));

		//set the posting list for daat and taat.
		setPostings(postings,term);
	}



	/**
	 * @param postings
	 * @param term
	 */
	private void setPostings(String postings, String term){

		String postingsData = postings.substring(2,postings.length()-1);

		LinkedList<Document> docIdList = new LinkedList<Document>();
		LinkedList<Document> termFreqList = new LinkedList<Document>();		 
		//split the data
		String [] dataArr = postingsData.split(",");		

		for(int i = 0; i < dataArr.length; i++){

			String[] data = dataArr[i].split("/");

			Integer documentId = Integer.valueOf(data[0].trim());
			Integer termFrequency = Integer.valueOf(data[1].trim());
			Document document = new Document();
			document.setDocumentId(documentId);
			document.setTermFrequency(termFrequency);

			docIdList.add(document);

			termFreqList.add(document); 
		}

		//sort the docIdList based on documentIds.
		Collections.sort(docIdList,new DocumentIdSortComparator());

		//sort the termFreqList based on termFrequencies.
		termFreqList.sort(new TermFrequencySortComparator());

		taat_postings.put(term, termFreqList);
		daat_postings.put(term, docIdList);

	}



	/**
	 * Get posting list for the given terms
	 * 
	 * @param queryTerms
	 */
	private void getPostings(String[] queryTerms){

		for(String term :queryTerms){

			out.write("\n"+"FUNCTION: getPostings "+term);

			if(daat_postings.get(term) == null){
				out.write("\n"+"terms not found");	
			}else{

				out.write("\n");

				// get posting list ordered by docIds.
				List<Document> postings_ordered_by_docIds = daat_postings.get(term);
				int size = postings_ordered_by_docIds.size();
				out.write("Ordered By doc IDs: ");

				for(int i = 0; i < size ; i++){

					out.write(postings_ordered_by_docIds.get(i).getDocumentId().toString());
					if(i < (size-1)){
						out.write(",");
					}		        	
				}

				// get posting list ordered by term frequencies

				List<Document> postings_ordered_by_term_frequencies = taat_postings.get(term);
				out.write("\n"+"Ordered By TF: ");
				for(int i = 0; i < size ; i++){
					out.write(postings_ordered_by_term_frequencies.get(i).getDocumentId().toString());
					if(i < (size-1)){
						out.write(",");
					}		        	
				}
			}
		}	
	}


	/*
	 *  Get the top k terms in the map.
	 * 
	 */
	@SuppressWarnings("unused")
	private void getTopKTerms(int K){

		/** sort the document frequency map based on values **/	
		out.write("Function: getTopK "+K);

		List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(document_frequency_map.entrySet());

		Collections.sort(entryList, new Comparator(){
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		int count = 0;
		out.write("\n"+"Result: ");
		for (Map.Entry<String, Integer> entry : entryList.subList(0, K)) {

			out.write(entry.getKey());
			if(count < K-1)
				out.write(",");
			count++;                    

		}

	}

	/**
	 * Check if term exists in the index for intersection
	 * @param terms
	 * @return
	 */
	private boolean checkTermsExistsInIndex(String[] terms){

		boolean doesNotExistFlag = false;

		for(String term: terms){
			if(!daat_postings.containsKey(term)){				
				doesNotExistFlag = true;
				break;				
			}					
		}
		return doesNotExistFlag;
	}

	/**
	 * 
	 * Gets common docIds between two using term_at_a_time_index.
	 * @param queryTerms
	 */
	private void termAtATimeQueryAnd(String[] queryTerms, boolean flag){		

		if(!flag)
			displayFNameAndTerms("termAtATimeQueryAnd", queryTerms);

		/** check if all term exists ***/
		if(!flag && checkTermsExistsInIndex(queryTerms)){
			out.write("\n"+"terms not found");			  
		}else if(flag && checkTermsExistsInIndex(queryTerms)){
			return;
		}else{
			List<Document> first_term_posting_list = null;
			
			List allPostingsList = new ArrayList();
			
			for(String term : queryTerms){				
				LinkedList<Document> postingList = taat_postings.get(term);
				allPostingsList.add(postingList);
				
			}
			
			if(flag){	
				/*** for optimization ***/
				Collections.sort(allPostingsList,new PostingListSizeComparator());								
			}			     
			
			first_term_posting_list = (LinkedList)allPostingsList.get(0);

			List<Document> result = null;		  
			int comparisonCount = 0;  
			long startTime = System.currentTimeMillis();
			long endTime = 0;

			for(int i = 1; i < allPostingsList.size(); i++){

				/*** get the next term postingList **/
				List<Document> next_term_posting_list = (LinkedList<Document>)allPostingsList.get(i);

				Iterator<Document> itr1 = first_term_posting_list.iterator();

				result =  new ArrayList<Document>();

				/** search for each documentId in next term posting list ***/

				while(itr1.hasNext()){

					Document doc = itr1.next();
					Integer docId_1 = doc.getDocumentId();
					for(int j = 0; j < next_term_posting_list.size(); j++){
						comparisonCount++;
						Document doc2 = next_term_posting_list.get(j);
						Integer docId_2 = doc2.getDocumentId();
						if(docId_1.equals(docId_2)){
							result.add(doc);
							break;
						}
					}
				}	

				first_term_posting_list = new ArrayList(result);
			}

			endTime = System.currentTimeMillis();
			float executionTime =  ((endTime - startTime) /1000);

			if(!flag){
				out.write("\n"+result.size()+" documents are found");
				out.write("\n"+comparisonCount+" comparisons are made");
				out.write("\n"+executionTime+" seconds are used");
			}else{
				
				out.write("\n"+comparisonCount+" comparisons are made with optimization");
				List<Integer> documentIds = new ArrayList<Integer>(result.size());
				for(int i = 0 ; i < result.size() ; i++){
					
					documentIds.add(result.get(i).getDocumentId());
				}
				Collections.sort(documentIds);
				printResult(documentIds);
			}
			

		}

	}



	/**
	 * 
	 * Gets union of documentIds for all the terms using term_at_a_time index.
	 * @param queryTerms
	 */
	private void termAtATimeQueryOr(String[] queryTerms, boolean flag){

		if(!flag)
		   displayFNameAndTerms("termAtATimeQueryOr", queryTerms);

		/*** find the first term which is present in the postings ***/
		List<Document> first_term_posting_list = null;
		
		List<LinkedList> allPostingsList = new ArrayList<LinkedList>();
		
		for(String term : queryTerms){				
			LinkedList<Document> postingList = taat_postings.get(term);
			if(postingList != null)
			   allPostingsList.add(postingList);
		}
				
		if(flag){
			Collections.sort(allPostingsList,new PostingListSizeComparator());			
		}

		if(!flag && allPostingsList.size() == 0){			
			out.write("\n"+"terms not found");
		}else if(flag && allPostingsList.size() == 0){
			return;
		}else{
			
			first_term_posting_list = new LinkedList<Document>(allPostingsList.get(0));
			
			List<Integer> unionResult = new ArrayList();

			for(Document doc : first_term_posting_list){				
				unionResult.add(doc.getDocumentId());				
			}

			int comparisonCount = 0; 
			Iterator<Document> itr = null;

			long startTime = System.currentTimeMillis();
			long endTime = 0;

			for(int i = 1; i < allPostingsList.size(); i++){

				LinkedList<Document> next_term_posting_list = allPostingsList.get(i);
				if(next_term_posting_list != null && next_term_posting_list.size() > 0){

					itr = next_term_posting_list.iterator();

					int count = 0;
					while(itr.hasNext()){
						
						Document doc  = itr.next();
						Integer docId = doc.getDocumentId();
						boolean value_present = false;

						/** check if the docId is already present in the result 
						 *  if it is not present, add it to the result list.
						 * **/
						for(int j = 0; j < unionResult.size()-count ; j++){
							Integer docId2 = unionResult.get(j);
							comparisonCount++;
							if(docId.equals(docId2)){							
								value_present = true;
								break;
							}

						}

						if(!value_present){						
							unionResult.add(docId);
							count = count + 1;
						}
					}
				}		  
			}

			endTime = System.currentTimeMillis();
			float executionTime =  ((endTime - startTime) /1000);

			if(!flag){
				out.write("\n"+unionResult.size()+" documents are found");
				out.write("\n"+comparisonCount+" comparisons are made");
				out.write("\n"+executionTime+" seconds are used");
			}else{		
			out.write("\n"+comparisonCount+" comparisons made with optimization");
			// sort the result
			Collections.sort(unionResult);
			printResult(unionResult);

			}
		}
	}


	/**
	 * display function name and terms
	 * @param functionName
	 * @param queryTerms
	 */
	private void displayFNameAndTerms(String functionName, String[] queryTerms){

		out.write("\n"+"FUNCTION: "+functionName+" ");
		for(int i=0; i < queryTerms.length; i++){
			out.write(queryTerms[i]);
			if(i < (queryTerms.length - 1))
				out.write(",");
		}
	}

	/**
	 * display documentIds
	 * @param result
	 */
	private void printResult(List<Integer> result){

		out.write("\n"+"Result: ");
		for(int i = 0; i < result.size(); i++){
			out.write(result.get(i).toString());
			if(i < (result.size()-1))
				out.write(",");
		}
	}


	/**
	 * Get the common documentIds between terms using document_at_a_time index.
	 * @param queryTerms
	 */
	private void docAtATimeQueryAnd(String[] queryTerms){

		displayFNameAndTerms("docAtATimeQueryAnd", queryTerms);

		/** check if all term exists ***/
		if(checkTermsExistsInIndex(queryTerms)){
			out.write("\n"+"terms not found");			  
		}else{

			ArrayList<LinkedList<Document>> postingList = new ArrayList(queryTerms.length);

			/*** get all the postingList for all the terms ***/		
			for(String term : queryTerms){
				postingList.add(daat_postings.get(term));
			}

			/*** initialize pointers ***/		
			int[] pointer_arr = new int[postingList.size()];

			/** initialize all the pointers to 0 **/;		
			for(int i = 0 ; i < pointer_arr.length ; i++){
				pointer_arr[i] = 0;
			}	

			List<Integer> documentIds = null;

			List<Integer> intersection_result = new ArrayList<Integer>();

			Integer[] max_length = new Integer[postingList.size()];
			int max_index = setMaximumSize(postingList,max_length);

			int comparisonCount = 0;

			long startTime = System.currentTimeMillis();
			long endTime = 0;

			while(pointer_arr[max_index] < max_length[max_index]){

				documentIds = new ArrayList<Integer>();  
				for(int k = 0 ; k < postingList.size() ; k++){

					/** get list of all documentIds for the current pointer position ***/
					if(pointer_arr[k] != max_length[k]){

						Document doc =  postingList.get(k).get(pointer_arr[k]);
						documentIds.add(doc.getDocumentId()); 	

					}	
				}

				if(documentIds.size() == 1 || documentIds.size() < postingList.size())
					break;

				/** get the comparison count, set the result and increment pointers ****/
				comparisonCount = comparisonCount + setPointers(documentIds,pointer_arr,intersection_result); 
			}


			endTime = System.currentTimeMillis();
			float executionTime =  ((endTime - startTime) /1000);

			out.write("\n"+intersection_result.size()+" documents are found");
			out.write("\n"+comparisonCount+" comparisons are made");
			out.write("\n"+executionTime+" seconds are used");

			Collections.sort(intersection_result);
			printResult(intersection_result);

		}
	}


	/**
	 * For setting the maximum length of each pointers.
	 * @param postingList
	 * @param max_length
	 * @return
	 */
	private int setMaximumSize(List<LinkedList<Document>> postingList,Object[] max_length){

		int max_size = postingList.get(0).size();
		max_length[0] = max_size;
		int max_index = 0;
		for(int i = 1 ;  i < postingList.size() ; i++){

			if(postingList.get(i).size() > max_size){

				max_index = i;
			}			
			max_length[i] = postingList.get(i).size();
		}

		return max_index;
	}


	/**
	 * @param documentIds
	 * @param pointer_arr
	 * @param final_result
	 */
	private int setPointers(List<Integer> documentIds,int[] pointer_arr,List<Integer> final_result){

		int comparisonCount = 0;

		if(documentIds != null && documentIds.size() > 0){
			int max_value = documentIds.get(0);
			boolean all_equal = true;

			/*** get the max value between documentIds ***/

			for(int i = 1 ; i < documentIds.size() ; i++){	
				if(documentIds.get(i) > max_value){
					comparisonCount++;
					max_value = documentIds.get(i);
				}		
			}

			/*** find out if all are equal ***/

			for(int i = 0 ; i < documentIds.size() ; i++){	
				if(documentIds.get(i) != max_value){
					all_equal = false;
					comparisonCount++;
					break;
				}
			}

			if(all_equal){
				/** increment each pointer in the array **/
				final_result.add(max_value);
				for(int j = 0 ; j < pointer_arr.length ; j++){
					pointer_arr[j] = pointer_arr[j] + 1;
				}
			}else{
				/*** all values are not equal and we need to increment pointers for some ***/
				for(int k = 0 ; k < documentIds.size() ; k++){
					if(documentIds.get(k) < max_value){
						comparisonCount++;
						pointer_arr[k] = pointer_arr[k] + 1;
					}	
				}
			}
		}

		return comparisonCount;
	}


	/**
	 * Get the union of documentIds between terms using document_at_a_time index. 
	 * @param terms
	 */
	private void docAtATimeQueryOr(String[] queryTerms){

		displayFNameAndTerms("docAtATimeQueryOr", queryTerms);

		ArrayList<LinkedList<Document>> postingsList = new ArrayList(queryTerms.length);

		/*** get all the postingList for all the terms ***/		
		for(String term : queryTerms){
			if(daat_postings.get(term) != null)
				postingsList.add(daat_postings.get(term));
		}

		if(postingsList.size() == 0){			
			out.write("\n"+"terms not found");
		}else{

			int original_size = postingsList.size();

			/*** initialize pointers ***/		
			Object[] pointer_arr = new Object[postingsList.size()];

			/** initialize all the pointers to 0 **/;		
			for(int i = 0 ; i < pointer_arr.length ; i++){
				pointer_arr[i] = 0;
			}	

			List<Integer> documentIds = null;
			List<Integer> union_result = new ArrayList<Integer>();

			/*** set the maximum size of each pointer ***/
			Object[] max_length = new Object[postingsList.size()];
			setMaximumSize(postingsList,max_length);

			/*** created list type of each pointer for removal when 
			 the pointers have reached their max length ***/
			List<Integer> pointer_arr_list = new ArrayList(Arrays.asList(pointer_arr));
			List<Integer> max_length_arr_list = new ArrayList(Arrays.asList(max_length));

			int comparisonCount = 0;

			long startTime = System.currentTimeMillis();
			long endTime = 0;

			if(postingsList != null && postingsList.size() > 0){
				while(postingsList.size() > 0){
					documentIds = new ArrayList<Integer>();

					/*** get the list of all documentIds at the current pointer position ***/

					for(int k = 0 ; k < postingsList.size() ; k++){
						if(pointer_arr[k] != max_length[k]){

							Document doc =  postingsList.get(k).get((Integer)pointer_arr[k]);
							documentIds.add(doc.getDocumentId()); 	

						}		
					}	

					/*** finds the minimum of all documentIds and adds it to the result
					 *   increments the pointer and returns the comparisonCount
					 *  ****/

					comparisonCount = comparisonCount + findMinimum(documentIds,union_result,pointer_arr,pointer_arr_list); 

					/***
					 *  a) remove the indexed list whose pointer has reached max length 
					 *     from the postingList
					 *  b) also remove the pointer index from the pointer array and max_length array.     
					 *  
					 * ***/
					for (int i = 0; i < pointer_arr_list.size() ; i++){					
						if(pointer_arr_list.get(i).equals(max_length_arr_list.get(i))){
							postingsList.remove(i);
							pointer_arr_list.remove(i);
							max_length_arr_list.remove(i);						   
						}
					}			


					if(original_size != postingsList.size()){
						pointer_arr = pointer_arr_list.toArray();
						max_length =  max_length_arr_list.toArray();
					}	

				}	
			}

			endTime = System.currentTimeMillis();
			float executionTime =  ((endTime - startTime) /1000);

			out.write("\n"+union_result.size()+" documents are found");
			out.write("\n"+comparisonCount+" comparisons are made");
			out.write("\n"+executionTime+" seconds are used");
			printResult(union_result);

		}

	}



	/**
	 * find minimum value for document_at_time_or function.
	 * @param documentIds
	 * @param pointer_arr
	 * @param final_result
	 */
	private int findMinimum(List<Integer> documentIds,List<Integer> union_result,Object[] pointer_arr,List<Integer> pointer_arr_list){

		int comparisonCount = 0;
		if(documentIds != null && documentIds.size() > 0){
			int min_value = documentIds.get(0);
			int min_value_index = 0;

			/** find the minimum documentId ***/
			for(int i = 1 ; i < documentIds.size() ; i++){
				comparisonCount++;
				if(documentIds.get(i) < min_value){
					min_value = documentIds.get(i);					
					min_value_index = i;
				}
			}

			/** add the minimum value to result **/
			if(union_result != null && union_result.size() > 0){

				/** minimum value should be greater 
				 *  than the last element in the result
				 */
				if(min_value > union_result.get(union_result.size()-1))
					union_result.add(min_value);

			}else{
				union_result.add(min_value);
			}

			/** increment the pointer of min_value **/
			pointer_arr[min_value_index] =  (Integer)pointer_arr[min_value_index] + 1;

			/*** increment in the arraylist also ***/
			pointer_arr_list.set(min_value_index, (Integer)pointer_arr_list.get(min_value_index)+1);
		}

		return comparisonCount;
	}

}
