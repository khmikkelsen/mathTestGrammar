
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

import java.io.*;
import java.rmi.server.ExportException;


public class Main {

    public static void main(String[] args) throws IOException {
        CharStream is = CharStreams.fromString("");
        MathLexer lexer = new MathLexer(is);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MathParser parser = new MathParser(tokenStream);

        try {

            MathParser.CompileUnitContext cst = parser.compileUnit();
            ExpressionNode ast = new BuildAstVisitor().visitCompileUnit(cst);
            Double value = new EvaluateExpressionVisitor().visit(ast);
            System.out.println(value.toString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}

abstract class ExpressionNode{

}

abstract class InfixExpressionNode extends ExpressionNode{
    public ExpressionNode left;

    public ExpressionNode getLeft() {
        return left;
    }

    public void setLeft(ExpressionNode left) {
        this.left = left;
    }

    public ExpressionNode right;

    public ExpressionNode getRight() {
        return right;
    }

    public void setRight(ExpressionNode right) {
        this.right = right;
    }
}

class AdditionNode extends InfixExpressionNode{

}

class SubtractionNode extends InfixExpressionNode{

}

class MultiplicationNode extends InfixExpressionNode{

}

class DivisionNode extends InfixExpressionNode{

}

class NegateNode extends ExpressionNode{

    public ExpressionNode InnerNode;

    public NegateNode(ExpressionNode innerNode) {
        InnerNode = innerNode;
    }

    public ExpressionNode getInnerNode() {
        return InnerNode;
    }

    public void setInnerNode(ExpressionNode innerNode) {
        InnerNode = innerNode;
    }
}

class NumberNode extends ExpressionNode{
    public NumberNode(double value) {
        Value = value;
    }

    public double Value;

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }
}

class BuildAstVisitor extends MathBaseVisitor<ExpressionNode> {

    @Override
    public ExpressionNode visitCompileUnit(MathParser.CompileUnitContext context) {
        //return super.visitCompileUnit(context.expr());
        return super.visit(context.expr());
    }

    @Override
    public ExpressionNode visitNumberExpr(MathParser.NumberExprContext context) {
        return new NumberNode(Double.valueOf(context.value.getText()));
    }

    @Override
    public ExpressionNode visitParensExpr(MathParser.ParensExprContext context) {
        //return super.visitParensExpr(context.expr());
        return super.visit(context.expr());
    }

    @Override
    public ExpressionNode visitInfixExpr(MathParser.InfixExprContext context){
        InfixExpressionNode node = new InfixExpressionNode() {};

        switch (context.op.getType()) {
            case MathLexer.OP_ADD:
                node = new AdditionNode();
                break;
            case MathLexer.OP_SUB:
                node = new SubtractionNode();
                break;
            case MathLexer.OP_MUL:
                node = new MultiplicationNode();
                break;
            case MathLexer.OP_DIV:
                node = new DivisionNode();
                break;

                default:
                    System.out.println("Error");

        }


        node.left = visit(context.left);
        node.right = visit(context.left);

        return node;
    }

    @Override
    public ExpressionNode visitUnaryExpr(MathParser.UnaryExprContext context) {

        switch (context.op.getType()) {
            case MathLexer.OP_ADD:
                return visit(context.expr());

            case MathLexer.OP_SUB:
                return new NegateNode(visit(context.expr()));

            default:
                System.out.println("Error 2");
                //return super.visitUnaryExpr(ctx);
        }
        return null;
    }

}

abstract class AstVisitor<T>{
    public abstract T visit(AdditionNode node) throws InvalidTypeException;
    public abstract T visit(SubtractionNode node) throws InvalidTypeException;
    public abstract T visit(MultiplicationNode node) throws InvalidTypeException;
    public abstract T visit(DivisionNode node) throws InvalidTypeException;
    public abstract T visit(NegateNode node) throws InvalidTypeException;
    public abstract T visit(NumberNode node);

    public T visit(ExpressionNode node) throws InvalidTypeException {
        if (node instanceof AdditionNode){
            return visit((AdditionNode)node);
        } else if (node instanceof SubtractionNode){
            return visit((SubtractionNode)node);
        }else if (node instanceof MultiplicationNode){
            return visit((MultiplicationNode)node);
        }else if (node instanceof DivisionNode){
            return visit((DivisionNode)node);
        }else if (node instanceof NegateNode){
            return visit((NegateNode)node);
        }else if(node instanceof NumberNode){
            return visit((NumberNode)node);
        }
        throw new InvalidTypeException();
    }
}

class EvaluateExpressionVisitor extends AstVisitor<Double>{
    @Override
    public Double visit(AdditionNode node) throws InvalidTypeException {
        return visit(node.left) + visit(node.right);
    }

    @Override
    public Double visit(SubtractionNode node) throws InvalidTypeException {
        return visit(node.left) - visit(node.right);
    }

    @Override
    public Double visit(MultiplicationNode node) throws InvalidTypeException {
        return visit(node.left) * visit(node.right);
    }

    @Override
    public Double visit(DivisionNode node) throws InvalidTypeException {
        return visit(node.left) / visit(node.right);
    }

    @Override
    public Double visit(NegateNode node) throws InvalidTypeException {
        return -visit(node.InnerNode);
    }

    @Override
    public Double visit(NumberNode node) {
        return node.Value;
    }
}