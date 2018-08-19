library(XML)
library(plyr)

# download sitelist from EC
download.file("http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/siteList.xml", "sitelist-ec-current.xml")

# convert to UTF-8
outf <- file("sitelist-ec-current-utf8.xml", encoding="UTF-8")
writeLines(iconv(readLines("sitelist-ec-current.xml"), from = "ISO-8859-1", to = "UTF8"), outf)
close(outf)
rm(outf)

newsitelist <- xmlToList(xmlParse("sitelist-ec-current-utf8.xml"))
oldsitelist <- xmlToList(xmlParse("sitelist-base-v2.xml"))

newsitedf <- ldply(newsitelist, data.frame, stringsAsFactors=FALSE)
oldsitedf <- ldply(oldsitelist, data.frame, stringsAsFactors=FALSE)

# cleanup
rm(newsitelist)
rm(oldsitelist)

# rename ".attrs" column to "sitecode"
names(newsitedf)[names(newsitedf)==".attrs"] <- "sitecode"
names(oldsitedf)[names(oldsitedf)==".attrs"] <- "sitecode"

# any old sites that are no longer in new sites?
sites_to_remove <- oldsitedf[!(oldsitedf$sitecode %in% newsitedf$sitecode) & (oldsitedf$.id == "site"),]
stopifnot(nrow(sites_to_remove) == 0)

# find differences between sites
newsites <- newsitedf[!(newsitedf$sitecode %in% oldsitedf$sitecode),]
# add columns that are in oldsitedf and not in newsitedf
newsites <- cbind(newsites, data.frame(lat=NA, lon=NA, webId=NA))

# get lat/lon for each site
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

# assign new web IDs
new_web_ids <- c(
  "Richmond BC" = "bc-96",
  "Duncan BC" = "bc-97",
  "Qualicum Beach BC" = "bc-98",
  "West Vancouver BC" = "bc-99",
  "Langley BC" = "bc-100"
)

newsites$webId <- new_web_ids[paste(newsites$nameEn, newsites$provinceCode)]

# combine old list with updated new list
finallist <- rbind(oldsitedf, newsites)

saveRDS(finallist, file="sitelist-base-v3.rds")
