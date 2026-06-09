package de.srh_dr.mediamanagementtoolmmt.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AltTitleTest {

    @Test
    void testSetValidTitle(){
        AltTitle altTitle = new AltTitle(0, "test", true);
        altTitle.setTitle("Result ");
        assertEquals("Result", altTitle.getTitle());
    }

    @Test
    void testSetInvalidTitle(){
        AltTitle altTitle = new AltTitle(0, "test", false);
        try {
            altTitle.setTitle(" ");
        }catch(NullPointerException ignored){}
        assertEquals("test", altTitle.getTitle());
    }
}