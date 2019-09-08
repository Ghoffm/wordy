package wordy.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import wordy.ast.AssignmentNode;
import wordy.ast.BinaryExpressionNode;
import wordy.ast.BlockNode;
import wordy.ast.ConditionalNode;
import wordy.ast.ConstantNode;
import wordy.ast.VariableNode;

import static wordy.parser.ShaderParser.parseExpression;
import static wordy.parser.ShaderParser.parseProgram;
import static wordy.parser.ShaderParser.parseStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserTest {

    @Test
    void testNumbers() {
        assertEquals(new ConstantNode(37),    parseExpression("37"));
        assertEquals(new ConstantNode(37.2),  parseExpression("37.2"));
        assertEquals(new ConstantNode(-37.2), parseExpression("-37.2"));
        assertParseError(() -> parseExpression("-37..2"));
    }

    @Test
    void testOperators() {
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.ADDITION,
                new ConstantNode(1),
                new ConstantNode(2)),
            parseExpression("1 plus 2"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.SUBTRACTION,
                new ConstantNode(1),
                new ConstantNode(2)),
            parseExpression("1 minus 2"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.MULTIPLICATION,
                new ConstantNode(1),
                new ConstantNode(2)),
            parseExpression("1 times 2"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.DIVISION,
                new ConstantNode(1),
                new ConstantNode(2)),
            parseExpression("1 divided by 2"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.DIVISION,
                new ConstantNode(1),
                new ConstantNode(2)),
            parseExpression("1\ndivided    by\n 2"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.EXPONENTIATION,
                new ConstantNode(2),
                new ConstantNode(8)),
            parseExpression("2 to the power of 8"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.EXPONENTIATION,
                new ConstantNode(16),
                new ConstantNode(2)),
            parseExpression("16 squared"));
    }

    @Test
    void testPrecedence() {
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.ADDITION,
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.SUBTRACTION,
                    new ConstantNode(1),
                    new ConstantNode(2)),
                new ConstantNode(3)),
            parseExpression("1 minus 2 plus 3"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.SUBTRACTION,
                new ConstantNode(1),
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.DIVISION,
                    new ConstantNode(2),
                    new ConstantNode(3))),
            parseExpression("1 minus 2 divided by 3"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.DIVISION,
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.SUBTRACTION,
                    new ConstantNode(1),
                    new ConstantNode(2)),
                new ConstantNode(3)),
            parseExpression("(1 minus 2) divided by 3"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.EXPONENTIATION,
                new VariableNode("x"),
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.EXPONENTIATION,
                    new VariableNode("y"),
                    new VariableNode("z"))),
            parseExpression("x to the power of y to the power of z"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.EXPONENTIATION,
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.EXPONENTIATION,
                    new VariableNode("x"),
                    new ConstantNode(2)),
                new ConstantNode(2)),
            parseExpression("x squared squared"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.EXPONENTIATION,
                new VariableNode("y"),
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.EXPONENTIATION,
                    new VariableNode("z"),
                    new ConstantNode(2.0))),
            parseExpression("y to the power of z squared"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.SUBTRACTION,
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.ADDITION,
                    new BinaryExpressionNode(
                        BinaryExpressionNode.Operator.EXPONENTIATION,
                        new VariableNode("x"),
                        new ConstantNode(2.0)),
                    new BinaryExpressionNode(
                        BinaryExpressionNode.Operator.MULTIPLICATION,
                        new ConstantNode(2.0),
                        new BinaryExpressionNode(
                            BinaryExpressionNode.Operator.EXPONENTIATION,
                            new VariableNode("y"),
                            new BinaryExpressionNode(
                                BinaryExpressionNode.Operator.EXPONENTIATION,
                                new VariableNode("z"),
                                new ConstantNode(2.0))))),
                new ConstantNode(1.0)),
            parseExpression("x squared plus 2 times y to the power of z squared minus 1"));
    }

    @Test
    void testVariables() {
        assertEquals(new VariableNode("x"), parseExpression("x"));
        assertEquals(new VariableNode("snake_and_camelcase_ok"), parseExpression("snake_and_camelCase_OK"));
        assertEquals(
            new BinaryExpressionNode(
                BinaryExpressionNode.Operator.ADDITION,
                new VariableNode("x"),
                new VariableNode("y")),
            parseExpression("x plus y"));
    }

    @Test
    void testAssignmentStatement() {
        assertEquals(
            new AssignmentNode(
                new VariableNode("sprongle"),
                new ConstantNode(-3)),
            parseStatement("set sprongle to -3"));
        assertEquals(
            new AssignmentNode(
                new VariableNode("sprongle"),
                new BinaryExpressionNode(
                    BinaryExpressionNode.Operator.ADDITION,
                    new VariableNode("zoink"),
                    new ConstantNode(7))),
            parseStatement("set sprongle to zoink plus 7"));
    }

    @Test
    void testConditional() {
        assertEquals(
            new ConditionalNode(
                ConditionalNode.Operator.EQUALS,
                new VariableNode("x"),
                new ConstantNode(2),
                parseStatement("set y to 1"),
                parseStatement("set z to 3")),
            parseStatement("if x is equal to 2 then set y to 1 else set z to 3"));
        assertEquals(
            new ConditionalNode(
                ConditionalNode.Operator.EQUALS,
                new VariableNode("x"),
                new ConstantNode(2),
                parseStatement("set y to 1"),
                BlockNode.EMPTY),
            parseStatement("if x equals 2 then set y to 1"));
        assertEquals(
            new ConditionalNode(
                ConditionalNode.Operator.EQUALS,
                new VariableNode("x"),
                new ConstantNode(2),
                new BlockNode(
                    parseStatement("set y to 1"),
                    parseStatement("set z to 3")),
                new BlockNode(
                    parseStatement("set a to 1"),
                    parseStatement("set b to 3"))),
            parseStatement("if x equals 2 then:set y to 1.set z to 3.else:set a to 1.set b to 3.end of conditional"));
        assertEquals(
            new ConditionalNode(
                ConditionalNode.Operator.EQUALS,
                new VariableNode("x"),
                new ConstantNode(2),
                new BlockNode(
                    parseStatement("set y to 1"),
                    parseStatement("set z to 3")),
                BlockNode.EMPTY),
            parseStatement("if x is equal to 2 then  :  \nset y to 1.\nset z to 3.\n\nend  of conditional"));
    }

    @Test
    void testProgram() {
        assertParseError(() -> parseProgram(""));
        assertEquals(
            new BlockNode(
                new AssignmentNode(
                    new VariableNode("x"),
                    new ConstantNode(1))),
            parseProgram("set x to 1."));

        for(var whitespaceVariant : List.of(
            "set x to 1.set y to 2.",
            "set x to 1. set y to 2.",
            " \n  set x to 1.\n\n   \n\tset y to 2.   \n  ")
        ) {
            assertEquals(
                new BlockNode(
                    new AssignmentNode(
                        new VariableNode("x"),
                        new ConstantNode(1)),
                    new AssignmentNode(
                        new VariableNode("y"),
                        new ConstantNode(2))),
                parseProgram(whitespaceVariant));
        }
    }

    private void assertParseError(Runnable parseAction) {
        try {
            parseAction.run();
            fail("Expected test to raise exception");
        } catch(ParseException e) {
            // success!
        }
    }
}
