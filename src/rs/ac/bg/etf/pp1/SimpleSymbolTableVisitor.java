/**
 *
 */
package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

/**
 * @author nemanja.kojic
 *
 */
public class SimpleSymbolTableVisitor extends SymbolTableVisitor {

    protected StringBuilder output = new StringBuilder();
    protected final String indent = "   ";
    protected StringBuilder currentIndent = new StringBuilder();

    private int visitingClassDepth = 0;
    private int classUnrollLimit = 2;
    private boolean simplePrint;

    SimpleSymbolTableVisitor(boolean simplePrint) {
        this.simplePrint = simplePrint;
    }

    protected void nextIndentationLevel() {
        currentIndent.append(indent);
    }

    protected void previousIndentationLevel() {
        if (currentIndent.length() > 0)
            currentIndent.setLength(currentIndent.length()-indent.length());
    }

    @Override
    public void visitObjNode(Obj objToVisit) {
        switch (objToVisit.getKind()) {
            case Obj.Con:
                output.append("Con ");
                break;
            case Obj.Var:
                output.append("Var ");
                break;
            case Obj.Type:
                output.append("Type ");
                break;
            case Obj.Meth:
                output.append("Meth ");
                break;
            case Obj.Fld:
                output.append("Fld ");
                break;
            case Obj.Prog:
                output.append("Prog ");
                break;
        }

        output.append(objToVisit.getName());
        output.append(": ");

        if ((Obj.Var == objToVisit.getKind()) && "this".equalsIgnoreCase(objToVisit.getName()))
            output.append("");
        else
            objToVisit.getType().accept(this);

        output.append(", ");
        output.append(objToVisit.getAdr());
        output.append(", ");
        output.append(objToVisit.getLevel() + " ");

        if (!simplePrint) {
            if (objToVisit.getKind() == Obj.Prog || objToVisit.getKind() == Obj.Meth) {
                output.append("[");
                output.append("\n");
                nextIndentationLevel();
            }

            for (Obj o : objToVisit.getLocalSymbols()) {
                output.append(currentIndent.toString());
                o.accept(this);
                output.append("\n");
            }

            if (objToVisit.getKind() == Obj.Prog || objToVisit.getKind() == Obj.Meth) {
                previousIndentationLevel();
                output.append(currentIndent.toString());
                output.append("]");
            }
        }
    }

    @Override
    public void visitScopeNode(Scope scope) {
        for (Obj o : scope.values()) {
            o.accept(this);
            output.append("\n");
        }
    }

    @Override
    public void visitStructNode(Struct structToVisit) {
        switch (structToVisit.getKind()) {
            case Struct.None:
                output.append("notype");
                break;
            case Struct.Int:
                output.append("int");
                break;
            case Struct.Char:
                output.append("char");
                break;
            case Struct.Array:
                output.append("Arr of ");

                switch (structToVisit.getElemType().getKind()) {
                    case Struct.None:
                        output.append("notype");
                        break;
                    case Struct.Int:
                        output.append("int");
                        break;
                    case Struct.Char:
                        output.append("char");
                        break;
                    case Struct.Class:
                        output.append("Class");
                        break;
                    case StructExt.Bool:
                        output.append("bool");
                        break;
                }
                break;
            case Struct.Class:
                output.append("Class");
                if (!simplePrint) {
                    if (visitingClassDepth < classUnrollLimit) {
                        visitingClassDepth++;
                        output.append("[");
                        output.append("\n");
                        nextIndentationLevel();
                        for (Obj o : structToVisit.getMembers().symbols()) {
                            output.append(currentIndent.toString());
                            o.accept(this);
                            output.append("\n");
                        }
                        previousIndentationLevel();
                        output.append(currentIndent.toString());
                        output.append("]");
                        visitingClassDepth--;
                    } else {
                        output.append("[ unroll limit reached ]");
                    }
                }
                break;
            case StructExt.Bool:
                output.append("bool");
                break;
        }

    }

    public String getOutput() {
        return output.toString();
    }


}
