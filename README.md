# & PluralCraft

A 1.19+ server side quilt mod for plural systems.

## todo

- implement coloured names
- investigate unsticking latch during preview
- skin proxying?
- nameplate changes
- better usage instructions

## installation

install the mod jar into the server. for preview support enable chat previews in the server.properties file
```properties
previews-chat=true
```

## Usage

create a system by running `/pc create`

- the list of current members can be viewed with `/pc member`
- members can be created with `/pc member new <name>`
- members can be managed with `/pc member <name> <subcommand>`
    - members can be deleted with `delete`
    - members display names can be queried with `display` and set with `display <name>`
    - members proxies can be listed with `proxy`, added with `proxy add <tag>`, and removed with `proxy remove <tag>`
- the current fronter can be set with `/pc front [fronter]` where no fronter prints the current fronter and `clear` clears the fronter
- autoproxy can be configured with `/pc autoproxy <type>`


made with ❤️ by foxes
