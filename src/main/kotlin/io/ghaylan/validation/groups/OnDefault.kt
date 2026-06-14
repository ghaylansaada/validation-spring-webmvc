package io.ghaylan.validation.groups

/**
 * Default validation group marker interface applied when no explicit group is specified.
 *
 * All validation constraints automatically belong to this group by default unless an
 * explicit group targeting strategy (such as [OnCreate]) is declared.
 */
interface OnDefault