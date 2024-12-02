# Automate tool

## Common schema.

1. init: an ordered list of actions run first.
2. vars: a object of static variables as `name=>value`.

## Special schema for global.conf.

1. cases: a string point to a folder contains case configuration.

## Special schema for case configuration.

1. actions: an ordered list of actions to run.
2. order: an integer that as natural order of cases.
3. cleanup: a boolean value that remove all context variables after execution.

## Action schema

1. action: the action name.
2. other: other configuration keys for each action type.
