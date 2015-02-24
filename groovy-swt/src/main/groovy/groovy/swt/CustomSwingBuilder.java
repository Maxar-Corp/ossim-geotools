/*
 * Created on Dec 15, 2004
 *
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id: CustomSwingBuilder.java 15346 2009-02-11 12:12:40Z frank.tolstrup $
 */
package groovy.swt;

import groovy.swing.SwingBuilder;

/**
 * @author ckl
 *
 */
public class CustomSwingBuilder extends SwingBuilder {

    /* 
     * override to make public
     * 
     * @see groovy.util.BuilderSupport#getCurrent()
     */
    public Object getCurrent() {
        return super.getCurrent();
    }
}