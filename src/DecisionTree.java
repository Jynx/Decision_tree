import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
 
/**
 * This class provides a framework for accessing a decision tree.
 * Put your code in constructor, printInfoGain(), buildTree and buildPrunedTree()
 * You can add your own help functions or variables in this class 
 */
public class DecisionTree {
    /**
     * training data set, pruning data set and testing data set
     */
    private DataSet mTrain = null;        // Training Data Set
    private DataSet mTune = null;        // Tuning Data Set
    private DataSet mTest = null;        // Testing Data Set
    public String mClassification;
    public List<Instance> mTrainSet = null;    
    boolean caseLabel1 = false; 
    String mDefaultChoice;
    DecTreeNode mRoot;
    
    class CountInfo {
        int mLabel1Sum = 0;
        int mLabel2Sum = 0;
        int[][] mLabel1Count;
        int[][] mLabel2Count;
        int mTotalCombinedLabelCount = 0;
        ArrayList<informationObject> mInformationGain = new ArrayList<informationObject>();
    }
 
    /**
     * Constructor
     * 
     * @param train  
     * @param tune
     * @param test
     */
    DecisionTree(DataSet train, DataSet tune, DataSet test) {
        mTrain = train;
        mTune = tune;
        mTest = test;
    }
    
    /**
     * print information gain of each possible question at root node.
     * 
     */
    public void printInfoGain() {
        countData(this.mTrain, true, new ArrayList<String>());
    }
    /**
     * This method sorts through all of the data and keeps
     * counters of both edible and poisonous mushrooms in their
     * respective 2d arrays. 
     */
    
    private CountInfo countData(DataSet data, boolean print, ArrayList<String> ignoredAttrs) {
        CountInfo countInfo = new CountInfo();
        List<Instance> instanceList = data.instances; 
        countInfo.mTotalCombinedLabelCount = instanceList.size();
        Instance currInstance = null; 
        countInfo.mLabel1Count = new int[data.attr_name.length][150];
        countInfo.mLabel2Count = new int[data.attr_name.length][150]; // check length
        for (int i = 0; i < instanceList.size(); i++) {
            currInstance = instanceList.get(i);
            mClassification = currInstance.label;
            List<String> attributes = currInstance.attributes;          
            if (mClassification.equals(data.labels[0])) {
                countInfo.mLabel1Sum++;
                caseLabel1 = true;
            } else {
                countInfo.mLabel2Sum++;
                caseLabel1 = false;    
            }
            for(int j = 0; j < attributes.size(); j++ ) {
                String attribute = attributes.get(j);
                    for(int m = 0; m < data.attr_val[j].length; m++) {
                        if (data.attr_val[j][m].equals(attribute)) {
                            if (caseLabel1) {
                                countInfo.mLabel1Count[j][m]++; 
                            } else {
                                countInfo.mLabel2Count[j][m]++;
                            }
                        }
                    }
            }      
        }    
        calculateEntropy(countInfo, print, data, ignoredAttrs);
        Collections.sort(countInfo.mInformationGain);
        return countInfo;
    }
    /**
     * Calculates entropy for every attribute, followed by creating a list of said entropies
     * in descending order from greatest to least. 
     */
    private void calculateEntropy(CountInfo countInfo, boolean print, 
            DataSet data, ArrayList<String> ignoredAttrs) {
       double label1, label2 = 0.0; 
       label1 = (double)countInfo.mLabel1Sum / (double)countInfo.mTotalCombinedLabelCount; 
       label2 = (double)countInfo.mLabel2Sum / (double)countInfo.mTotalCombinedLabelCount;
       double classEntropy = 0;
       if (label1 == 0 && label2 == 0) {
           classEntropy = 0;
       } else if (label1 == 0 && label2 != 0) {
           classEntropy = -label2 * Math.log(label2) / Math.log(2);   
       } else if (label1 != 0 && label2 == 0) {
           classEntropy = (-label1) * (Math.log(label1)) / Math.log(2);       
       } else {
           classEntropy = (-label1) * (Math.log(label1)) / Math.log(2) - 
               label2 * Math.log(label2) / Math.log(2);       
       }
       for (int i = 0; i < data.attr_name.length; i++) {
           if (!ignoredAttrs.contains(data.attr_name[i])) {
               countInfo.mInformationGain.add(calculateConditionalEntropy(data.attr_name[i], classEntropy,
                       countInfo.mLabel1Count[i], countInfo.mLabel2Count[i], print, countInfo.mTotalCombinedLabelCount));
           }
       }
    }   
     
    
    /** 
     * Helper Method for calculateEntropy that will, for a given attribute, find the relative amounts of
     * label examples, calculate individual entropy, store that value in an array, and then
     * sum all of those calculations to subtract from the class entropy for a total conditional entropy
     * calculation. 
     * @param classEntropy - class entropy
     * @param label1Row - number of edible shrooms
     * @param label2Row - number of poisonous shrooms. 
     * @return
     */
    private informationObject calculateConditionalEntropy(String name, double classEntropy, 
            int[] label1Row, int[] label2Row, boolean print, double totalCombinedLabelCount) {
        double attrLabel1Total = 0.0, attrLabel2Total = 0.0, attrExamplesTotal = 0.0;
        double attributeEntropy = 0.0, attributeTotal = 0.0;
        informationObject infoObj = null;
        int indexOfAttribute = 0; 
        Double[] calculatedValue = new Double [label1Row.length];
        for (int i = 0; i < label1Row.length; i++) {
            if (label1Row[i] == 0 || label2Row[i] == 0) {
                continue;
            }
            attrLabel1Total = (double)label1Row[i];
            attrLabel2Total = (double)label2Row[i];
            attrExamplesTotal = attrLabel1Total + attrLabel2Total;
            double coefficient = (attrExamplesTotal/totalCombinedLabelCount);
            double label1Calc = -(attrLabel1Total/attrExamplesTotal) * (Math.log(attrLabel1Total/attrExamplesTotal)) / Math.log(2);
            double label2Calc = -(attrLabel2Total/attrExamplesTotal) * (Math.log(attrLabel2Total/attrExamplesTotal)) / Math.log(2);
            double combinedLabels = (label1Calc + label2Calc);
            double finalCalc = coefficient * combinedLabels;
 
            calculatedValue[i] = finalCalc;
        }        
        for (int j = 0; j < calculatedValue.length; j++) {
            if (calculatedValue[j] == null) {
                continue;
            }
            attributeEntropy += calculatedValue[j];
        }
        attributeTotal = classEntropy - attributeEntropy;
        
        for (int k = 0; k < mTrain.attr_val.length; k++) {
           if (name.equals(mTrain.attr_name[k])) {
               indexOfAttribute = k;
               break;
            }
        }
        infoObj = new informationObject(name, attributeTotal, indexOfAttribute);
        if (print) {
            System.out.println(name + " info gain = " + String.format("%.3f", attributeTotal));
        }
        return infoObj;
        
    }
    /**
     * Build a decision tree given only a training set.
     * 
     */
    public void buildTree() {  
        ArrayList<String> ignoredAttrs = new ArrayList<String>();
        CountInfo countInfo = countData(mTrain, false, ignoredAttrs);
        mRoot = new DecTreeNode(null, countInfo.mInformationGain.get(0).getAttributeName(), 
                "ROOT", false, mTrain);
        mDefaultChoice = calculateDefault(mTrain, countInfo.mLabel1Sum, countInfo.mLabel2Sum);
        buildDecisionTree(mRoot, mTrain, ignoredAttrs);
           
    }
    private void buildDecisionTree(DecTreeNode parent, DataSet data, ArrayList<String> ignoredAttrs) {
        CountInfo countInfo = countData(data, false, ignoredAttrs);
        parent.terminal = countInfo.mLabel1Sum == 0 || countInfo.mLabel2Sum == 0;
        
        if (countInfo.mInformationGain.size() == 0) {
            parent.label = mDefaultChoice;
            return;
        }
        
        parent.attribute = countInfo.mInformationGain.get(0).getAttributeName();
        int index = countInfo.mInformationGain.get(0).getIndex();
        
        if (parent.terminal) {
            if (countInfo.mLabel2Sum == 0 && countInfo.mLabel1Sum == 0) {         
                parent.label = mDefaultChoice;
                return;
            } else if (countInfo.mLabel1Sum == 0 && countInfo.mLabel2Sum != 0) {
                parent.label = data.labels[1];
                return;
            } else if (countInfo.mLabel2Sum == 0 && countInfo.mLabel1Sum != 0) {        
                parent.label = data.labels[0];
                return;
            }   
        }
        
        ArrayList<String> copyIgnoredAttrs = new ArrayList<String>();
        copyIgnoredAttrs.addAll(ignoredAttrs);
        copyIgnoredAttrs.add(parent.attribute);
        List<Instance> instanceList = data.instances, newInstanceList = null;
        List<String> attributes = null;     
        Instance instance = null; 
        String[] attributeRow = mTrain.attr_val[index];
        for (int j = 0; j < attributeRow.length; j++) {
            newInstanceList = new ArrayList<Instance>();
            for (int k = 0; k < instanceList.size(); k++) {
                instance = instanceList.get(k);
                attributes = instance.attributes;
                if (attributes.get(index).equals(attributeRow[j])) {                        
                    newInstanceList.add(instance);
                }
            }
            if (newInstanceList.size() > 0) {
                DataSet newDataSet = new DataSet();
                newDataSet.instances = newInstanceList;
                newDataSet.labels = data.labels.clone();     
                newDataSet.attr_val = data.attr_val.clone();
                newDataSet.attr_name = data.attr_name.clone();
                
                DecTreeNode child = new DecTreeNode(null, null, attributeRow[j], false, newDataSet);
                parent.addChild(child);
                buildDecisionTree(child, newDataSet, copyIgnoredAttrs);
            }
        }
    }
    
    public String calculateDefault(DataSet data, double label1Sum, double label2Sum) {
        Instance curInstance = data.instances.get(0);
        if (label1Sum > label2Sum) {
            return mTrain.labels[0]; 
        } else if (label1Sum < label2Sum) {
            return mTrain.labels[1];
        } else {
            return curInstance.label;
        }
    }
    
 
    /**
     * Build a decision tree given a training set then prune it using a tuning set.
     * 
     */
    public void buildPrunedTree() {
 
        // TODO: add code here
        
    }
 
    
  /**
   * Evaluates the learned decision tree on a test set.
   * @return the label predictions for each test instance 
   *     according to the order in data set list
   */
    public String[] classify() {
        String[] predictions = new String[mTest.instances.size()];
        String question = null;
        DecTreeNode currNode = null, tmpNode = null;
        for (int i = 0; i < predictions.length; i++) {
            Instance currInstance = mTest.instances.get(i);
            currNode = mRoot;
            String label = "";
            while (currNode != null) {
                question = currNode.attribute;
                int attrIndex = 0;
                for( int j = 0; j < currNode.data.attr_name.length; j++) {
                    if (question.equals(currNode.data.attr_name[j])) {
                        attrIndex = j;
                        break;
                    }
                }
                List<DecTreeNode> nodelist = currNode.children;
                currNode = null;
                for (int x = 0; x < nodelist.size(); ++x) {
                    tmpNode = nodelist.get(x);
                    if (tmpNode.parentAttributeValue.equals(currInstance.attributes.get(attrIndex))) {
                        currNode = tmpNode;
                        label = currNode.label;
                        break;
                    }
                }
            }
            if (label == null) {
                predictions[i] = mDefaultChoice;
            } else {
                predictions[i] = label;
            }
        }    
        return predictions;
    }
 
    /**
     * Prints the tree in specified format. It is recommended, but not
     * necessary, that you use the print method of DecTreeNode.
     * 
     * Example:
     * Root {odor?}
     *     a (e)
     *     m (e)
        *       n {habitat?}
     *         g (e)
     *         l (e)
     *       p (p)
        *       s (e)
     *         
     */
    public void print() {
        mRoot.print(1);
    }
}