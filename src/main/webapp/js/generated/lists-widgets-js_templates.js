// This file was automatically generated from lists-widgets-js.soy.
// Please don't edit this file by hand.

goog.provide('blue.templates.lists.widgets');

goog.require('soy');
goog.require('soy.StringBuilder');


/**
 * @param {Object.<string, *>=} opt_data
 * @param {soy.StringBuilder=} opt_sb
 * @return {string|undefined}
 * @notypecheck
 */
blue.templates.lists.widgets.list = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div listId="', soy.$$escapeHtml(opt_data.listId), '" class="list"><p>List ID ', soy.$$escapeHtml(opt_data.listId), ', owned by ', soy.$$escapeHtml(opt_data.ownerId), ':</p><ul>');
  var memberList10 = opt_data.members;
  var memberListLen10 = memberList10.length;
  for (var memberIndex10 = 0; memberIndex10 < memberListLen10; memberIndex10++) {
    var memberData10 = memberList10[memberIndex10];
    output.append('<li class="brandListItem">');
    blue.templates.lists.widgets.member(memberData10, output);
    output.append('</li>');
  }
  output.append('</ul></div>');
  if (!opt_sb) return output.toString();
};


/**
 * @param {Object.<string, *>=} opt_data
 * @param {soy.StringBuilder=} opt_sb
 * @return {string|undefined}
 * @notypecheck
 */
blue.templates.lists.widgets.member = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('\t<p>User ID ', soy.$$escapeHtml(opt_data.id), '</p>');
  if (!opt_sb) return output.toString();
};
