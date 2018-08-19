#r script to write sitelist-base-v3.xml

library(XML)

finallist <- readRDS("sitelist-base-v3.rds")

root <- newXMLNode("siteList", namespaceDefinitions = c(fw="http://apps.fishandwhistle.net/schemas/ecweather"))

plyr::a_ply(finallist, .margins=1, .fun=function(row) {
  if(row$.id == "province") {
    node <- newXMLNode("fw:province")
  } else {
    node <- newXMLNode(row$.id)
  }
  
  xmlAttrs(node) <- c(code=row$sitecode)
  for(name in names(row)) {
    if(!is.na(row[[name]])) {
      if(name %in% c("nameEn", "nameFr", "provinceCode")) {
        childnode <- newXMLNode(name)
      } else if(name %in% c("webId", "lat", "lon")) {
        childnode <- newXMLNode(paste0("fw:", name))
      } else {
        next
      }
      
      xmlValue(childnode) <- row[[name]]
      addChildren(node, childnode)
    }
  }
  
  addChildren(root, node)
})

saveXML(newXMLDoc(node=root), file="sitelist-base-v3.xml")
