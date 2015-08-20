package tilestore.security

import grails.plugin.springsecurity.ui.SecurityUiTagLib

class SecurityTagLib extends SecurityUiTagLib {
    static defaultEncodeAs = "raw" //[taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "tilestore"

    def submitButton = { attrs ->
		String form = getRequiredAttribute(attrs, 'form', 'submitButton')
		String elementId = getRequiredAttribute(attrs, 'elementId', 'submitButton')
		String text = resolveText(attrs)

		def writer = getOut()
		writer << """<a id="${elementId}" """
		writeRemainingAttributes writer, attrs
		writer << ">${text}</a>\n"
		writer << "<input type='submit' value=' ' id='${elementId}_submit' class='s2ui_hidden_button' />\n"

		String javascript = """
			\$("#${elementId}").button();
			\$('#${elementId}').bind('click', function() {
				
				var password = \$('#password').val();
				var confirmPassword = \$('#confirmPassword').val();


				if ( password === confirmPassword ) {
			   		document.forms.${form}.submit();
				} else {
					alert("Passwords do not match, please try again");
					\$('#password').val('');
					\$('#confirmPassword').val('');	
				}

			});
		
		"""
		writeDocumentReady writer, javascript
	}

	def confirmPassword = { attrs ->

		String htmlElement = """

		<tr class="prop">
				<td valign="top" class="name">
					<label for="confirmPassword">Confirm Password</label>
				</td>
				<td valign="top" class="value ">
					<input type="password" name="confirmPassword" value="" id="confirmPassword" />

				</td>
			</tr>
		
		"""
		out<<htmlElement
	}



}
