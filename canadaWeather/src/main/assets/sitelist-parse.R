library(XML)
library(plyr)

#download sitelist from EC
download.file("http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/siteList.xml", "sitelist-ec-current.xml")

#convert to UTF-8
outf <- file("sitelist-ec-current-utf8.xml", encoding="UTF-8")
writeLines(iconv(readLines("sitelist-ec-current.xml"), from = "ISO-8859-1", to = "UTF8"), outf)
close(outf)
rm(outf)

newsitelist <- xmlToList(xmlParse("sitelist-ec-current-utf8.xml"))
oldsitelist <- xmlToList(xmlParse("sitelist-base-v1.xml"))

newsitedf <- ldply(newsitelist, data.frame, stringsAsFactors=FALSE)
oldsitedf <- ldply(oldsitelist, data.frame, stringsAsFactors=FALSE)

#cleanup
rm(newsitelist)
rm(oldsitelist)

#rename ".attrs" column to "sitecode"
names(newsitedf)[names(newsitedf)==".attrs"] <- "sitecode"
names(oldsitedf)[names(oldsitedf)==".attrs"] <- "sitecode"

#find differences between sites
newsites <- newsitedf[!(newsitedf$sitecode %in% oldsitedf$sitecode),]
#add columns that are in oldsitedf and not in newsitedf
newsites <- cbind(newsites, data.frame(lat=NA, lon=NA, webId=NA))

#get lat/lon for each site
for(i in 1:nrow(newsites)) {
  url <- paste0("http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/",
                newsites$provinceCode[i], "/", 
                newsites$sitecode[i], "_e.xml")
  download.file(url, "tmp.xml")
  location <- xmlToList(xmlParse("tmp.xml"))$location
  newsites$lat[i] <- gsub("[NW]", "", location$name$.attrs['lat'])
  newsites$lon[i] <- paste0("-", gsub("[NW]", "", location$name$.attrs['lon'])) #all will be negative
}
unlink("tmp.xml")
rm(i)
rm(location)
rm(url)

#eskasoni, NS (s0000861) has webId ns-44
newsites$webId[newsites$sitecode=="s0000861"] <- "ns-44"

#combine old list with updated new list
finallist <- rbind(oldsitedf, newsites)

#add "HEF" (appears to be BC Highway) to province list
finallist <- rbind(finallist[1:13,], 
                   data.frame(.id="province", 
                              nameEn="BC Highway", 
                              nameFr="Autoroute BC", 
                              sitecode="HEF",
                              provinceCode=NA,
                              webId=NA,
                              lat=NA,
                              lon=NA), 
                   finallist[14:nrow(finallist),])



#cleanup variables
rm(newsitedf)
rm(newsites)
rm(oldsitedf)

save(finallist, file="sitelist-base-v2.RData")
