package statementGraph.graphNode;


import org.eclipse.jdt.core.dom.ForStatement;


public class ForStatementItem extends ElementItem{

	private ForStatement astNode; 
	
	private ElementItem bodyEntry;
	
	public void setBodyEntry(ElementItem item){
		this.bodyEntry = item;
	}
	
	public ElementItem getBodyEntry(){
		return this.bodyEntry;
	}
	
	public ForStatementItem(ForStatement astNode){
		this.astNode = astNode;
		super.setType(astNode.getNodeType());
		this.setLineCount(astNode.toString());
	}
	
	public ForStatement getASTNode(){
		return this.astNode;
	}
	
	@Override
	protected void setLineCount(String code) {
		//It should be the length excluding the body.
		int total = code.split(System.getProperty("line.separator")).length;
		int body = astNode.getBody().toString().split(System.getProperty("line.separator")).length;
		super.lineCount = total - body; //Maybe problematic, check again! 
	}
	
	@Override
	public void printName() {
		System.out.print("For Statement: "+astNode.toString());
	}

	@Override
	public void printDebug() {
		System.out.print("For Statement: "+astNode.toString());
		System.out.println("Successor: -->");
		if(super.getCFGSeqSuccessor() == null){
			System.out.println("null");
		}
		else{
			super.getCFGSeqSuccessor().printName();
		}
		System.out.println("Body entry: -->");
		if(bodyEntry == null){
			System.out.println("null");
		}else{
			this.bodyEntry.printName();
		}
		super.printDDGPredecessor();
	}
}
