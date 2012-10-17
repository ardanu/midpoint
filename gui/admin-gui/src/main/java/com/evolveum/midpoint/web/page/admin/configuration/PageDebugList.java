/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.page.admin.configuration;

import com.evolveum.midpoint.common.QueryUtil;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.SubstringFilter;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.AjaxDownloadBehavior;
import com.evolveum.midpoint.web.component.button.AjaxLinkButton;
import com.evolveum.midpoint.web.component.button.AjaxSubmitLinkButton;
import com.evolveum.midpoint.web.component.button.ButtonType;
import com.evolveum.midpoint.web.component.data.RepositoryObjectDataProvider;
import com.evolveum.midpoint.web.component.data.TablePanel;
import com.evolveum.midpoint.web.component.data.column.ButtonColumn;
import com.evolveum.midpoint.web.component.data.column.CheckBoxHeaderColumn;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.component.dialog.ConfirmationDialog;
import com.evolveum.midpoint.web.component.option.OptionContent;
import com.evolveum.midpoint.web.component.option.OptionItem;
import com.evolveum.midpoint.web.component.option.OptionPanel;
import com.evolveum.midpoint.web.component.util.LoadableModel;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.web.page.admin.resources.dto.ResourceDto;
import com.evolveum.midpoint.web.security.MidPointApplication;
import com.evolveum.midpoint.web.security.WebApplicationConfiguration;
import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.SystemConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.UserType;
import com.evolveum.prism.xml.ns._public.query_2.QueryType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.PackageResourceStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author lazyman
 * @author mserbak
 */

public class PageDebugList extends PageAdminConfiguration {

    private static final Trace LOGGER = TraceManager.getTrace(PageDebugList.class);
    private static final String DOT_CLASS = PageDebugList.class.getName() + ".";
    private static final String OPERATION_DELETE_OBJECT = DOT_CLASS + "deleteObject";
    private static final String OPERATION_DELETE_OBJECTS = DOT_CLASS + "deleteObjects";
    private static final String OPERATION_SEARCH_OBJECT = DOT_CLASS + "searchObjects";
    private boolean deleteSelected;
    private boolean downloadZip;
    private IModel<ObjectTypes> choice = null;
    private ObjectType object = null;
    private List<?> objects;

    public PageDebugList() {
        initLayout();
    }

    private void initLayout() {
    	//confirm delete
    	add(new ConfirmationDialog("confirmDeletePopup", createStringResource("pageDebugList.dialog.title.confirmDelete"),
                createDeleteConfirmString()) {

            @Override
            public void yesPerformed(AjaxRequestTarget target) {
                close(target);
                if(deleteSelected){
                	deleteSelected = false;
                	deleteSelectedConfirmedPerformed(target);
                } else {
                	deleteObjectConfirmedPerformed(target);
                }
                
            }
        });
    	
        //listed type
    	IModel<ObjectTypes> sessionChoice = null;
    	PrismObjectDefinition selectedCategory = (PrismObjectDefinition)getSession().getAttribute("category");
    	if(selectedCategory != null) {
    		sessionChoice = new Model<ObjectTypes>(ObjectTypes.getObjectTypeFromTypeQName(selectedCategory.getTypeName()));
    	} else {
    		sessionChoice = new Model<ObjectTypes>(ObjectTypes.SYSTEM_CONFIGURATION);
    	}
    	final IModel<ObjectTypes> choice = sessionChoice;
    	loadObjects(choice.getObject(), null);

        List<IColumn<? extends ObjectType>> columns = new ArrayList<IColumn<? extends ObjectType>>();

        IColumn column = new CheckBoxHeaderColumn<ObjectType>();
        columns.add(column);

        column = new LinkColumn<SelectableBean<? extends ObjectType>>(createStringResource("pageDebugList.name"), "name", "value.name") {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<SelectableBean<? extends ObjectType>> rowModel) {
                ObjectType object = rowModel.getObject().getValue();
                objectEditPerformed(target, object.getOid());
            }
        };
        columns.add(column);

        column = new ButtonColumn<SelectableBean<? extends ObjectType>>(createStringResource("pageDebugList.operation"),
                createStringResource("pageDebugList.button.delete")) {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<SelectableBean<? extends ObjectType>> rowModel) {
                ObjectType object = rowModel.getObject().getValue();
                deleteObjectPerformed(target, choice, object);
            }
        };
        columns.add(column);

        final Form main = new Form("mainForm");
        add(main);
        
        OptionPanel option = new OptionPanel("option", createStringResource("pageDebugList.optionsTitle"), getPage(), false);
        option.setOutputMarkupId(true);
        main.add(option);

        OptionItem item = new OptionItem("search", createStringResource("pageDebugList.search"));
        option.getBodyContainer().add(item);
        IModel<String> searchNameModel = initSearch(item, choice);

        item = new OptionItem("category", createStringResource("pageDebugList.selectType"));
        option.getBodyContainer().add(item);
        initCategory(item, choice, searchNameModel);

        OptionContent content = new OptionContent("optionContent");
        main.add(content);
        
        Class provider = selectedCategory == null ? SystemConfigurationType.class : selectedCategory.getCompileTimeClass();
        TablePanel table = new TablePanel("table", new RepositoryObjectDataProvider(PageDebugList.this,
        		provider), columns);
        table.setOutputMarkupId(true);
        content.getBodyContainer().add(table);

        AjaxLinkButton delete = new AjaxLinkButton("deleteSelected", ButtonType.NEGATIVE,
                createStringResource("pageDebugList.button.deleteSelected")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteSelectedPerformed(target, choice);
            }
        };
        main.add(delete);
        
        final AjaxDownloadBehavior ajaxDownloadBehavior = new AjaxDownloadBehavior(true) {
            @Override
            protected File initFile() {
            	MidPointApplication application = getMidpointApplication();
				WebApplicationConfiguration config = application.getWebApplicationConfiguration();
				File folder = new File(config.getExportFolder());
				if (!folder.exists() || !folder.isDirectory()) {
					folder.mkdir();
				}
				
				String suffix = choice.getObject().getClassDefinition().getSimpleName() + "_"
						+ System.currentTimeMillis();
				File file = new File(folder, "ExportedData_" + suffix + ".xml");
				
				try {
					if(downloadZip) {
						file = createZipForDownload(file, folder, suffix);
					} else {
						file.createNewFile();
						createXmlForDownload(file);
					}
				} catch (Exception ex) {
					LoggingUtils.logException(LOGGER, "Couldn't init download link", ex);
				}
                return file;
            }
        };
        main.add(ajaxDownloadBehavior);
        
        
		AjaxLinkButton export = new AjaxLinkButton("exportAll",
				createStringResource("pageDebugList.button.exportAll")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
                ajaxDownloadBehavior.initiate(target);
			}
		};
        main.add(export);
        
        AjaxCheckBox zipCheck = new AjaxCheckBox("zipCheck", new Model<Boolean>(false)){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				downloadZip = !downloadZip;
			}
        	
        };
        main.add(zipCheck);
    }

    private IModel<String> initSearch(OptionItem item, final IModel<ObjectTypes> choice) {
        final IModel<String> model = new Model<String>();
        TextField<String> search = new TextField<String>("searchText", model);
        item.add(search);

        AjaxSubmitLinkButton clearButton = new AjaxSubmitLinkButton("clearButton",
                new StringResourceModel("pageDebugList.button.clear", this, null)) {

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                PageBase page = (PageBase) getPage();
                target.add(page.getFeedbackPanel());
            }

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                model.setObject(null);
                target.appendJavaScript("init()");
                target.add(PageDebugList.this.get("mainForm:option"));
                listObjectsPerformed(target, model.getObject(), choice.getObject());
            }
        };
        item.add(clearButton);

        AjaxSubmitLinkButton searchButton = new AjaxSubmitLinkButton("searchButton",
                new StringResourceModel("pageDebugList.button.search", this, null)) {

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                PageBase page = (PageBase) getPage();
                target.add(page.getFeedbackPanel());
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                listObjectsPerformed(target, model.getObject(), choice.getObject());
            }
        };
        item.add(searchButton);

        return model;
    }

    private void initCategory(OptionItem item, final IModel<ObjectTypes> choice, final IModel<String> searchNameModel) {
        IChoiceRenderer<ObjectTypes> renderer = new IChoiceRenderer<ObjectTypes>() {

            @Override
            public Object getDisplayValue(ObjectTypes object) {
                return new StringResourceModel(object.getLocalizationKey(),
                        (PageBase) PageDebugList.this, null).getString();
            }

            @Override
            public String getIdValue(ObjectTypes object, int index) {
                return object.getClassDefinition().getSimpleName();
            }
        };

        IModel<List<ObjectTypes>> choiceModel = createChoiceModel(renderer);
        final ListChoice listChoice = new ListChoice("choice", choice, choiceModel, renderer, choiceModel.getObject().size()) {

            @Override
            protected CharSequence getDefaultChoice(String selectedValue) {
                return "";
            }
        };
        listChoice.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(listChoice);
                listObjectsPerformed(target, searchNameModel.getObject(), choice.getObject());
            }
        });
        item.getBodyContainer().add(listChoice);
    }

    private IModel<List<ObjectTypes>> createChoiceModel(final IChoiceRenderer<ObjectTypes> renderer) {
        return new LoadableModel<List<ObjectTypes>>(false) {

            @Override
            protected List<ObjectTypes> load() {
                List<ObjectTypes> choices = new ArrayList<ObjectTypes>();
                Collections.addAll(choices, ObjectTypes.values());
                Collections.sort(choices, new Comparator<ObjectTypes>() {

                    @Override
                    public int compare(ObjectTypes o1, ObjectTypes o2) {
                        String str1 = (String) renderer.getDisplayValue(o1);
                        String str2 = (String) renderer.getDisplayValue(o2);
                        return String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
                    }
                });

                return choices;
            }
        };
    }

    private TablePanel getListTable() {
        OptionContent content = (OptionContent) get("mainForm:optionContent");
        return (TablePanel) content.getBodyContainer().get("table");
    }

    private RepositoryObjectDataProvider getDataTableProvider() {
        TablePanel tablePanel = getListTable();
        DataTable dataTable = tablePanel.getDataTable();
        return (RepositoryObjectDataProvider) dataTable.getDataProvider();
    }

    private void listObjectsPerformed(AjaxRequestTarget target, String nameText, ObjectTypes selected) {
        RepositoryObjectDataProvider provider = getTableDataProvider();
        ObjectQuery query = null;
        if (StringUtils.isNotEmpty(nameText)) {
            try {
				ObjectFilter substring = SubstringFilter.createSubstring(ObjectType.class, getPrismContext(),
						ObjectType.F_NAME, nameText);
                query = new ObjectQuery();
                query.setFilter(substring);
                provider.setQuery(query);
            } catch (Exception ex) {
                LoggingUtils.logException(LOGGER, "Couldn't create substring filter", ex);
                error(getString("pageDebugList.message.queryException", ex.getMessage()));
                target.add(getFeedbackPanel());
            }
        } else {
            provider.setQuery(null);
        }

        if (selected != null) {
            provider.setType(selected.getClassDefinition());
            loadObjects(selected, query);
        }
        
        TablePanel table = getListTable();
        target.add(table);
    }
    
    private void loadObjects(ObjectTypes selected, ObjectQuery query) {
    	Task task = createSimpleTask(OPERATION_SEARCH_OBJECT);
		OperationResult result = new OperationResult(OPERATION_SEARCH_OBJECT);
        try {
        	objects = getModelService().searchObjects(selected.getClassDefinition(), query, task, result);
		} catch (Exception ex) {
			LoggingUtils.logException(LOGGER, "Couldn't search objects", ex);
		}
    }

    private void objectEditPerformed(AjaxRequestTarget target, String oid) {
        PageParameters parameters = new PageParameters();
        parameters.add(PageDebugView.PARAM_OBJECT_ID, oid);
        setResponsePage(PageDebugView.class, parameters);
    }

    private RepositoryObjectDataProvider getTableDataProvider() {
        TablePanel tablePanel = getListTable();
        DataTable table = tablePanel.getDataTable();
        return (RepositoryObjectDataProvider<ObjectType>) table.getDataProvider();
    }
    
    private IModel<String> createDeleteConfirmString() {
        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
            	if(deleteSelected){
            		return createStringResource("pageDebugList.message.deleteSelectedConfirm",
                            WebMiscUtil.getSelectedData(getListTable()).size()).getString();
            	}
            	return createStringResource("pageDebugList.message.deleteObjectConfirm").getString();
            }
        };
    }
    
    private void deleteSelectedConfirmedPerformed(AjaxRequestTarget target){
    	MidPointApplication application = getMidpointApplication();
        RepositoryService repository = application.getRepository();
        ObjectTypes type = choice.getObject();

        OperationResult result = new OperationResult(OPERATION_DELETE_OBJECTS);
        List<SelectableBean<ObjectType>> beans = WebMiscUtil.getSelectedData(getListTable());
        for (SelectableBean<ObjectType> bean : beans) {
            ObjectType object = bean.getValue();
            OperationResult subResult = result.createSubresult(OPERATION_DELETE_OBJECT);
            try {
                repository.deleteObject(type.getClassDefinition(), object.getOid(), subResult);
                subResult.recordSuccess();
            } catch (Exception ex) {
                subResult.recordFatalError("Couldn't delete objects.", ex);
            }
        }
        result.recomputeStatus();

        RepositoryObjectDataProvider provider = getTableDataProvider();
        provider.clearCache();

        showResult(result);
        target.add(getListTable());
        target.add(getFeedbackPanel());
    }
    
    private void deleteObjectConfirmedPerformed(AjaxRequestTarget target){
    	MidPointApplication application = getMidpointApplication();
        RepositoryService repository = application.getRepository();

        OperationResult result = new OperationResult(OPERATION_DELETE_OBJECT);
        try {
            ObjectTypes type = choice.getObject();
            repository.deleteObject(type.getClassDefinition(), object.getOid(), result);
            result.recordSuccess();
        } catch (Exception ex) {
            result.recordFatalError("Couldn't delete object '" + object.getName() + "'.", ex);
        }

        RepositoryObjectDataProvider provider = getTableDataProvider();
        provider.clearCache();

        showResult(result);
        target.add(getListTable());
        target.add(getFeedbackPanel());
    }
    
    private void deleteSelectedPerformed(AjaxRequestTarget target, IModel<ObjectTypes> choice) {
    	List<SelectableBean<ObjectType>> selected = WebMiscUtil.getSelectedData(getListTable());
        if (selected.isEmpty()) {
            warn(getString("pageDebugList.message.nothingSelected"));
            target.add(getFeedbackPanel());
            return;
        }
        
    	ModalWindow dialog = (ModalWindow) get("confirmDeletePopup");
    	deleteSelected = true;
    	this.choice = choice;
        dialog.show(target);
    }

    private void deleteObjectPerformed(AjaxRequestTarget target, IModel<ObjectTypes> choice, ObjectType object) {
    	ModalWindow dialog = (ModalWindow) get("confirmDeletePopup");
    	this.choice = choice;
    	this.object = object;
        dialog.show(target);
    }
    
    private void createXmlForDownload(File file) {
    	OutputStreamWriter stream = null;
		try {
			stream = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
			if (objects != null) {
				String stringObject;
				stream.write(createHeaderForXml());
				for (Object object : objects) {
					if (!(object instanceof PrismObject)) {
						continue;
					}
					try {
						stringObject = getPrismContext().getPrismDomProcessor().serializeObjectToString(
								(PrismObject) object);
						stream.write(stringObject + "\n");
					} catch (Exception ex) {
						LOGGER.error("Failed to parse objects to string for xml. Reason:", ex);
					}
				}
			}
		} catch (Exception ex) {
			LoggingUtils.logException(LOGGER, "Couldn't create file", ex);
		}
		
		if (stream != null) {
			IOUtils.closeQuietly(stream);
		}
    }
    
    private File createZipForDownload(File file, File folder, String suffix) {
		File zipFile = new File(folder, "ExportedData_" + suffix + ".zip");
		OutputStreamWriter stream = null;
		ZipOutputStream out = null;
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("adding file " + file.getName() + " to zip archive");
			}
			out = new ZipOutputStream(new FileOutputStream(zipFile));
			final ZipEntry entry = new ZipEntry(file.getName());
			
			if (objects != null) {
				String stringObject;
				//entry.setSize(stringObject.length());
				out.putNextEntry(entry);
				out.write(createHeaderForXml().getBytes());
				for (Object object : objects) {
					if (!(object instanceof PrismObject)) {
						continue;
					}
					try {
						stringObject = getPrismContext().getPrismDomProcessor().serializeObjectToString(
								(PrismObject) object);
						out.write((stringObject + "\n").getBytes());
					} catch (Exception ex) {
						LOGGER.error("Failed to parse objects to string for zip. Reason:", ex);
					}
				}
				stream = new OutputStreamWriter(out, "utf-8");
			} else {
				entry.setSize(0);
				out.putNextEntry(entry);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("added file " + file.getName() + " to zip archive");
			}
		} catch (final IOException ex) {
			LOGGER.error("Failed to write to stream.", ex);
		} finally {
			if (null != stream) {
				try {
					out.finish();
					out.closeEntry();
					out.close();
					stream.close();
				} catch (final IOException ex) {
					LOGGER.error("Failed to pack file '" + file + "' to zip archive '" + out + "'", ex);
				}
			}
		}
		return zipFile;
	}
    
    private String parseObjectsToString(OutputStream stream) {
    	String result = createHeaderForXml();
    	if(objects != null) {
			for (Object object : objects) {
				if (!(object instanceof PrismObject)) {
					continue;
				}
				String stringObject = "";
				try {
					stringObject = getPrismContext().getPrismDomProcessor().serializeObjectToString(
							(PrismObject) object);
				} catch (Exception ex) {
					LOGGER.error("Failed to parse objects to string. Reason:", ex);
				}
				result += stringObject + "\n";
			}
		}
    	return result;
    }
    
    private String createHeaderForXml() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
	}
}
