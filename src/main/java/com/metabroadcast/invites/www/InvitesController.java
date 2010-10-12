package com.metabroadcast.invites.www;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.metabroadcast.invites.InviteRequestStore;

@Controller
public class InvitesController {

	private final InviteRequestStore invites;

	public InvitesController(InviteRequestStore invites) {
		this.invites = invites;
	}
	
	@RequestMapping(value="/invites", method=RequestMethod.GET)
	public String inviteForm(Map<String, Object> model, @RequestParam(defaultValue="false") boolean showError) {
		model.put("showError", showError);
		return "invites/form";
	}
	
	@RequestMapping(value="/invites", method=RequestMethod.POST)
	public String inviteSubmissionHandler(@RequestParam("screenName") String screenName) {
		try {
			invites.recordInviteRequest(screenName);
		} catch (Exception e) {
			return "redirect:/invites?showError=true";
		}
		return "redirect:/invites/thanks";
	}

	@RequestMapping(value="/invites/thanks", method=RequestMethod.GET)
	public String thanks() {
		return "invites/thanks";
	}
	
	@RequestMapping(value="/about", method=RequestMethod.GET)
    public String about() {
        return "invites/about";
    }
}
