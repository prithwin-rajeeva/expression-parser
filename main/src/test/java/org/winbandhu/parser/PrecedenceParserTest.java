package org.winbandhu.parser;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class PrecedenceParserTest {

    @Test public void testSimepleExpressions() {
        PrecedenceParser parser = new PrecedenceParser();
        assertEquals(true , parser.parse(new HashMap<String ,String>(),"true | true"));
        assertEquals(false , parser.parse(new HashMap<String ,String>(),"2 > 3"));
        assertEquals(false , parser.parse(new HashMap<String ,String>(),"(2 > 3) & true"));
        assertEquals(4 , ((Double)parser.parse(new HashMap<String ,String>(),"2 + 2")).intValue());
    }
}
