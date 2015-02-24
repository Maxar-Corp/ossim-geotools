/*
 * Created on Feb 20, 2004
 *
 */
package groovy.jface.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision: 915 $
 */
public class PreferencePageFieldEditorImpl extends FieldEditorPreferencePage {

    public class FieldEditorCreator {

        protected Class beanClass;
        protected String propertyName;
        protected String title;

        public FieldEditorCreator(Class beanClass, String propertyName,
                String title) {
            this.beanClass = beanClass;
            this.propertyName = propertyName;
            this.title = title;
        }

        public FieldEditor createField(Composite parent) {

            FieldEditor fieldEditor = null;

            try {
                Class[] types = { String.class, String.class, Composite.class};
                Constructor constructor = beanClass.getConstructor(types);
                if (constructor != null) {
                    Object[] arguments = { propertyName, title, parent};
                    fieldEditor = (FieldEditor) constructor
                            .newInstance(arguments);
                }
            } catch (Exception e) {
            }

            return fieldEditor;
        }
    }

    public class RadioGroupFieldEditorCreator extends FieldEditorCreator {
    	int numColumns;
    	String[][] labelAndValues;
    	boolean useGroup;
		public RadioGroupFieldEditorCreator(String propertyName, String title, int numColumns, String[][] labelAndValues, boolean useGroup) {
			super(RadioGroupFieldEditor.class, propertyName, title);
			this.numColumns = numColumns;
			this.labelAndValues = labelAndValues;
			this.useGroup = useGroup;
		}
    	
	    public FieldEditor createField(Composite parent) {
            FieldEditor fieldEditor = new RadioGroupFieldEditor(propertyName, title, numColumns, labelAndValues, parent, useGroup);
            return fieldEditor;
	    }
    	
    }

    
    
    
    private List creatorFieldsfields = new ArrayList();

    public PreferencePageFieldEditorImpl(String title) {
        super(title, FieldEditorPreferencePage.GRID);
    }

    public void addFieldCreator(Class beanClass, String propertyName,
            String title) {
        creatorFieldsfields.add(new FieldEditorCreator(beanClass, propertyName,
                title));
    }

    public void addRadioGroupFieldCreator(String propertyName, String title, int numColumns, String[][] labelAndValues, boolean useGroup) {
        creatorFieldsfields.add(new RadioGroupFieldEditorCreator(propertyName,
                title,numColumns, labelAndValues, useGroup));
    }
    
    
    protected void createFieldEditors() {
        Iterator i = creatorFieldsfields.iterator();
        while (i.hasNext()) {
            FieldEditorCreator creator = (FieldEditorCreator) i.next();
            FieldEditor fieldEditor = creator
                    .createField(getFieldEditorParent());
            addField(fieldEditor);
        }
    }

}
