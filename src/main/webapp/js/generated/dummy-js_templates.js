// This file was automatically generated from dummy-js.soy.
// Please don't edit this file by hand.

goog.provide('blue.templates.testing');

goog.require('soy');
goog.require('soy.StringBuilder');


/**
 * @param {Object.<string, *>=} opt_data
 * @param {soy.StringBuilder=} opt_sb
 * @return {string|undefined}
 * @notypecheck
 */
blue.templates.testing.aTemplate = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('Just testing!');
  if (!opt_sb) return output.toString();
};
