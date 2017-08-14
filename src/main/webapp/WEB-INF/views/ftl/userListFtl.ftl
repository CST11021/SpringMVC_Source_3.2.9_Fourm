<#import "spring.ftl" as spring />
<html>
   <head>
      <#--<title><@spring.message "website.title"/></title>-->
   </head>
   <body>
      <#--<@spring.message "user.userList.title"/>-->
      <table>
           <#list userList as user>
            <tr>
               <td>
                  <#--<a href="<@spring.url '/user/showUser/${user.userName}.html'/>">-->
                   ${user.userId}
                  <#--</a>-->
               </td>
                 <td>${user.userName}</td>
            </tr>
           </#list>
       <table>
   </body>
</html>