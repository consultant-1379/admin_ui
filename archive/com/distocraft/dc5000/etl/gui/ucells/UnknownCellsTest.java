/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.etl.gui.ucells;

import static org.junit.Assert.*;

import org.junit.Test;

public class UnknownCellsTest {

    @Test
    public void checkhashIdGenerator() throws Exception {

        final UnknownCells unknownCells = UnknownCells.class.newInstance();

        assertEquals("1805042870849258408",
                unknownCells.hashIdGenerator("1", "ONRM_RootMo_R:RNC01:RNC01", "", "ERICSSON", "HIER3_ID"));
        assertEquals("6337019889400695412",
                unknownCells.hashIdGenerator("1", "ONRM_RootMo_R:RNC01:RNC01", "", "ERICSSON", "HIER32_ID"));
        assertEquals("32388200577212841", unknownCells.hashIdGenerator("1", "ONRM_RootMo_R:RNC01:RNC01",
                "SAC-353-87-42444-54710", "ERICSSON", "HIER321_ID"));

        assertEquals("8432899184272901578", unknownCells.hashIdGenerator("0", "BSC1", "", "ERICSSON", "HIER3_ID"));
        assertEquals("1767008173202600382", unknownCells.hashIdGenerator("0", "BSC1", "", "ERICSSON", "HIER32_ID"));
        assertEquals("4900051584350354655",
                unknownCells.hashIdGenerator("0", "BSC1", "CELL-353-77-22-1140", "ERICSSON", "HIER321_ID"));
    }
}
