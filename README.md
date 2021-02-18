Simple test project / playground for [JBang](https://github.com/jbangdev/jbang)
Simple CLI tool to create and manage to-do lists for every days work.

### Features

- Create a new to-do list
- Add a new item to to-do list
- Check / uncheck item on to-do list with an optional comment
- List today's to-dos
- Written with [JBang](https://github.com/jbangdev/jbang) and [picoli](http://picocli.info/) cli.

### Edit Code

Edit with IntelliJ. How to prepare IntelliJ [link](https://www.jetbrains.com/help/idea/working-with-the-ide-features-from-command-line.html#arguments).

`jbang edit --open=idea .\Todo.java`

Edit with VScode. 

`jbang edit --open=code .\Todo.java`

You can also use` jbang edit --live Todo.java` and jbang will launch your editor while watching for file changes and regenerate the temporary project to pick up changes in dependencies.

### Run the command

Create new list.

`jbang Todo.java -n`

![](https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/images/new_list.png)

Add new item.

`jbang Todo.java -t "My new to-do 1"`

`jbang Todo.java -t "My new to-do 2"`

![](https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/images/new_list_item.png)

List items of latest to-do list.

`jbang Todo.java -l`

![](https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/images/list_items.png)

Check first item on latest to-do list. The comment is optional. The `-c` command toggles the check state.

`jbang Todo.java -c 1 "My comment on check."`

![](https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/images/check_item.png)

![](https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/images/uncheck_item.png)

### Run commands without checkout

`jbang https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/Todo.java -n`

`jbang https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/Todo.java -t "My new to-do 1"`

`jbang https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/Todo.java -l`

`jbang https://raw.githubusercontent.com/Xenexes/Todo-Cli/master/Todo.java -c 1 "My comment on check."`

### Run commands with alias