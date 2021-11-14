
import java.util.ArrayList;
import java.util.Iterator;
public class KDTree implements Iterable<Datum>{ 

	KDNode 		rootNode;
	int    		k; 
	int			numLeaves;

	// constructor

	public KDTree(ArrayList<Datum> datalist) throws Exception {

		Datum[]  dataListArray  = new Datum[ datalist.size() ];
		if (datalist.size() == 0) {
			throw new Exception("Trying to create a KD tree with no data");
		}
		else
			this.k = datalist.get(0).x.length;

		int ct=0;
		for (Datum d :  datalist) {
			dataListArray[ct] = datalist.get(ct);
			ct++;
		}

	//   Construct a KDNode that is the root node of the KDTree.

		rootNode = new KDNode(dataListArray);

		
		  this.numLeaves =rootNode.sumDepths_numLeaves()[1];
	}
	
	//   KDTree methods
	
	public Datum nearestPoint(Datum queryPoint) {
		return rootNode.nearestPointInNode(queryPoint);
	}
	

	public int height() {
		return this.rootNode.height();	
	}

	public int countNodes() {
		return this.rootNode.countNodes();	
	}
	
	public int size() {
		return this.numLeaves;
	}

	//-------------------  helper methods for KDTree   ------------------------------

	public static long distSquared(Datum d1, Datum d2) {

		long result = 0;
		for (int dim = 0; dim < d1.x.length; dim++) {
			result +=  (d1.x[dim] - d2.x[dim])*((long) (d1.x[dim] - d2.x[dim]));
		}
		// if the Datum coordinate values are large then we can easily exceed the limit of 'int'.
		return result;
	}

	public double meanDepth(){
		int[] sumdepths_numLeaves =  this.rootNode.sumDepths_numLeaves();
		return 1.0 * sumdepths_numLeaves[0] / sumdepths_numLeaves[1];
	}

	class KDNode { 

		boolean leaf;
		Datum leafDatum;           //  only stores Datum if this is a leaf
		
		//  the next two variables are only defined if node is not a leaf

		int splitDim;      // the dimension we will split on
		int splitValue;    // datum is in low if value in splitDim <= splitValue, and high if value in splitDim > splitValue  

		KDNode lowChild, highChild;   //  the low and high child of a particular node (null if leaf)
		  //  You may think of them as "left" and "right" instead of "low" and "high", respectively

		KDNode(Datum[] datalist) throws Exception{

			/*
			 *  This method takes in an array of Datum and returns 
			 *  the calling KDNode object as the root of a sub-tree containing  
			 *  the above fields.
			 */

		 

			ArrayList<Datum> lowDatalist = new ArrayList<Datum>();	// the low data set we will put as temporary
			ArrayList<Datum> highDatalist = new ArrayList<Datum>();	// the high data set we will put as temporary
			int countOfZeroDiff=0;	// the count of zero difference will check duplicates
			int difference =0;	// the difference will help to get split dimension

			if(datalist.length > 1) {
				// run loop dimension wise
				for (int i = 0; i < k; i++) {
					int maxOfDim =Integer.MIN_VALUE;
					int minOfDim =Integer.MAX_VALUE;
					int temp = 0;

					//	check the minimum and maximum valuve of considering dimension
					for (int j=0;j<datalist.length;j++){
						temp =datalist[j].x[i];
						if(temp<=minOfDim){
							minOfDim=temp;
						}
						if(temp>maxOfDim){
							maxOfDim=temp;
						}
					}

					// set split dimension
					if (difference <  maxOfDim-minOfDim) {
						difference = maxOfDim-minOfDim;
						splitDim = i;
						splitValue = (maxOfDim + minOfDim)/2;
						if(splitValue<0 || (splitValue == 0 && maxOfDim == 0)){
							splitValue--;
						}
					}

					// set count of zero dimension
					if (difference == 0) {
						countOfZeroDiff=countOfZeroDiff+1;
					}
				}
			}

			// check leaf node or not
			if(datalist.length == 1 || countOfZeroDiff==k){
				leaf = true ;
				leafDatum = datalist[0];
				lowChild = null;
				highChild =null;
			}
			else{
				for(int i=0; i<datalist.length;i++){
					if(datalist[i].x[splitDim]<=splitValue){
						lowDatalist.add(new Datum(datalist[i].x));
					}
					else{
						highDatalist.add(new Datum(datalist[i].x));
					}
				}
			}

			Datum[]  lowDataListArray  = new Datum[ lowDatalist.size() ];	// the low data will add to send to KDNode constructor
			Datum[]  highDataListArray  = new Datum[ highDatalist.size() ]; // the high data will add to send to KDNode constructor

			int count=0;
			//	add low data from temporary array
			for (Datum d :  lowDatalist) {
				lowDataListArray[count] = lowDatalist.get(count);
				count++;
			}
			count=0;
			//	add high data from temporary array
			for (Datum d :  highDatalist) {
				highDataListArray[count] = highDatalist.get(count);
				count++;
			}

			// send low and high data list array to KDNode constructor recursively
			if(!leaf) {
				lowChild = new KDNode(lowDataListArray);
				highChild = new KDNode(highDataListArray);
			}

		}

		public Datum nearestPointInNode(Datum queryPoint) {
			Datum nearestPoint, nearestPoint_otherSide;


			double qpTosplit,npToQp,npoToQp;
			double difNpToQp=0 ,difNpoToQp = 0;

			// Base case
			if(leaf){
				nearestPoint = leafDatum;
				return nearestPoint;
			}
			else{
				// get nearest point of quary point side
				if(queryPoint.x[splitDim]<=splitValue){
					nearestPoint = lowChild.nearestPointInNode(queryPoint);
				}else{
					nearestPoint = highChild.nearestPointInNode(queryPoint);
				}

				//get square difference of nearest point to quary point
				for (int i=0;i<k;i++){
					difNpToQp= difNpToQp + Math.pow(queryPoint.x[i]-nearestPoint.x[i],2);
				}

				//get difference of quary point split value
				qpTosplit = Math.sqrt(Math.pow(queryPoint.x[splitDim]-splitValue, 2));
				//get  difference of nearest point to quary point
				npToQp = Math.sqrt(difNpToQp);

				// check difference of nearest point to quary point is less or equal to difference of quary point to split
				if(npToQp<=qpTosplit){
					return nearestPoint;
				}
				else{
					// get nearest point of other side
					if(queryPoint.x[splitDim]<=splitValue){
						nearestPoint_otherSide = highChild.nearestPointInNode((queryPoint));
					}else{
						nearestPoint_otherSide = lowChild.nearestPointInNode(queryPoint);
					}

					//get square difference of other side nearest point to quary point
					for (int i=0;i<k;i++){
						difNpoToQp = difNpoToQp + Math.pow(queryPoint.x[i]-nearestPoint_otherSide.x[i],2);
					}

					//get  difference of other side nearest point to quary point
					npoToQp = Math.sqrt(difNpoToQp);

					// check difference of nearest point to quary point is less or equal to difference of other side nearest point to quary point
					if(npToQp<npoToQp){
						return nearestPoint;
					}
					return nearestPoint_otherSide;
				}
			}

		}
		
		// -----------------  KDNode helper methods (might be useful for debugging) -------------------

		public int height() {
			if (this.leaf) 	
				return 0;
			else {
				return 1 + Math.max( this.lowChild.height(), this.highChild.height());
			}
		}

		public int countNodes() {
			if (this.leaf)
				return 1;
			else
				return 1 + this.lowChild.countNodes() + this.highChild.countNodes();
		}
		
		/*  
		 * Returns a 2D array of ints.  The first element is the sum of the depths of leaves
		 * of the subtree rooted at this KDNode.   The second element is the number of leaves
		 * this subtree.    Hence,  I call the variables  sumDepth_size_*  where sumDepth refers
		 * to element 0 and size refers to element 1.
		 */
				
		public int[] sumDepths_numLeaves(){
			int[] sumDepths_numLeaves_low, sumDepths_numLeaves_high;
			int[] return_sumDepths_numLeaves = new int[2];
			
			/*     
			 *  The sum of the depths of the leaves is the sum of the depth of the leaves of the subtrees, 
			 *  plus the number of leaves (size) since each leaf defines a path and the depth of each leaf 
			 *  is one greater than the depth of each leaf in the subtree.
			 */
			
			if (this.leaf) {  // base case
				return_sumDepths_numLeaves[0] = 0;
				return_sumDepths_numLeaves[1] = 1;
			}
			else {
				sumDepths_numLeaves_low  = this.lowChild.sumDepths_numLeaves();
				sumDepths_numLeaves_high = this.highChild.sumDepths_numLeaves();
				return_sumDepths_numLeaves[0] = sumDepths_numLeaves_low[0] + sumDepths_numLeaves_high[0] + sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
				return_sumDepths_numLeaves[1] = sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
			}	
			return return_sumDepths_numLeaves;
		}
		
	}

	public Iterator<Datum> iterator() {
		return new KDTreeIterator();
	}
	
	private class KDTreeIterator implements Iterator<Datum> {
		

		public ArrayList<KDNode> stack = new ArrayList<KDNode>();

		// constructor

		private KDTreeIterator() {
			pushToLeft(rootNode);
		}

		// check has next leaf

		public boolean hasNext(){
			if(stack.isEmpty()){
				return false;
			}
			return true;
		}

		// return datum

		public Datum next(){
			KDNode leafNode = stack.get(stack.size()-1);
			stack.remove(stack.size()-1);
			if(!stack.isEmpty()) {
				KDNode PreNode = stack.get(stack.size()-1);
				stack.remove(stack.size()-1);
				pushToLeft(PreNode.highChild);
			}
			return leafNode.leafDatum;
		}

		// recursive add to list

		private void pushToLeft(KDNode node) {
			if (node != null) {
				stack.add(node);
				pushToLeft(node.lowChild);
			}
		}

	}

}

