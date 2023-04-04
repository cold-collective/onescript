package one.parser.rule;

import one.ast.NExpression;
import one.ast.NUnaryOp;
import one.lang.OneOperator;
import one.parser.ParseContext;
import one.parser.error.OneParseException;
import one.parser.token.BuiltInTokenTypes;
import one.parser.token.TokenType;

public class RFactor extends ParserRule<NExpression> {

    public RFactor() {
        super("factor", "exprFactor", 0);
    }

    @Override
    public boolean canParse(ParseContext context) {
        return true;
    }

    @Override
    public NExpression parseNode(ParseContext context) {
        // check for prefix unary operators
        if (context.currentType() == TokenType.OPERATOR) {
            OneOperator currentOp = context.current().getValueAs();
            OneOperator unaryOp = currentOp.toUnary();
            if (unaryOp == null)
                throw context.endOrHere(new OneParseException("operator " + currentOp + " does not apply as a unary operator"));
            context.next();
            // TODO: maybe do this through rules by calling ParseContext#tryParseNext
            //  once ive optimized that, as this is unintuitive for developers
            NExpression factor =
                    /* note that this refers to RFactor#parseNode,
                       so this will parse another factor, not any applicable rule */
                    this.parseNode(context);
            return new NUnaryOp(unaryOp, factor);
        }

        /* collect initial value */
        NExpression value;

        // check for literal
        value = context.tryParseNext("literal", NExpression.class);

        // check for expression
        if (context.currentType() == BuiltInTokenTypes.LEFT_PAREN) {
            context.next();
            value = context.tryParseNext("exprExpr", NExpression.class);
            if (context.currentType() != BuiltInTokenTypes.RIGHT_PAREN)
                throw context.endOrHere(new OneParseException("expected right parenthesis to close expression"));
            context.next();
        }

        // check for postfix unary operators
        if (context.currentType() == TokenType.OPERATOR) {
            OneOperator currentOp = context.current().getValueAs();
            OneOperator unaryOp = currentOp.toUnary();
            if (unaryOp == null)
                throw context.endOrHere(new OneParseException("operator " + currentOp + " does not apply as a unary operator"));
            context.next();
            return new NUnaryOp(unaryOp, value);
        } else {
            return value;
        }
    }

}
