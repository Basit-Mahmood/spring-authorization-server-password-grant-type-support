let $currentAccordianRow = null;

function findHiddenFieldInForm($form, hiddenFieldId, hiddenFieldName, hiddenFieldValue) {
	  			
	//let $formHiddenField = $form.find("input[type='hidden'][id='" + hiddenFieldId + "'][name='" + hiddenFieldName + "']");
	let $formHiddenField = $form.find("input[type='hidden'][name='" + hiddenFieldName + "']");
	if ($formHiddenField.length >= 1) {
		return $formHiddenField;
	}
	return null;
}

function addHiddenFieldToForm($form, hiddenFieldId, hiddenFieldName, hiddenFieldValue) {
	
	let $formHiddenField = findHiddenFieldInForm($form, hiddenFieldId, hiddenFieldName, hiddenFieldValue);
	if ($formHiddenField == null) {
		jQuery('<input>', {
			type: 'hidden',
		    id: hiddenFieldId,
		    name: hiddenFieldName,
		    value: hiddenFieldValue
		}).appendTo($form);
	}	
}

function removeHiddenFieldFromForm($form, hiddenFieldId, hiddenFieldName, hiddenFieldValue) {
	
	var $formHiddenField = findHiddenFieldInForm($form, hiddenFieldId, hiddenFieldName, hiddenFieldValue);
	if ($formHiddenField != null) {
		$formHiddenField.remove();
	}
		
}
