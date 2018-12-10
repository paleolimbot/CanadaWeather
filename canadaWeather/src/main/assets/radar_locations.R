
library(tidyverse)
library(curl)
library(rvest)

loc_java <- "/Users/dewey/d/android/CanadaWeather/canadaWeather/src/main/java/ca/fwe/caweather/radar/RadarLocations.java"
loc_assets <- "/Users/dewey/d/android/CanadaWeather/canadaWeather/src/main/assets"

text_java <- read_lines(loc_java)
all_en <- str_which(text_java, "ALL_EN =")
all_fr <- str_which(text_java, "ALL_FR =")
locs <- tibble(
  line_number = str_which(text_java, "new RadarLocation\\("),
  line = text_java[line_number],
  lang = if_else(line_number < all_fr, "EN", "FR")
) %>%
  separate(
    line,
    c("name", "alias", "id", "region", "lat", "lon", "updates"), 
    sep = ",",
    extra = "merge"
  ) %>%
  mutate_if(is.character, str_remove_all, '"|\\(|\\)|new LatLon|new RadarLocation|(,\\s*$)') %>%
  mutate_if(is.character, str_trim)

# isolate FR translations
names <- locs %>%
  select(lang, id, name) %>%
  spread(lang, name) %>%
  distinct(EN, FR) %>%
  rename(name_en = EN, name_fr = FR)
regions <- locs %>%
  select(lang, id, region) %>%
  spread(lang, region) %>%
  distinct(EN, FR) %>%
  rename(region_en = EN, region_fr = FR)
aliases <- locs %>%
  select(lang, id, alias) %>%
  spread(lang, alias) %>%
  distinct(EN, FR) %>%
  rename(alias_en = EN, alias_fr = FR)

locs_en <- locs %>% 
  filter(lang == "EN") %>%
  select(-lang) %>%
  rename(name_en = name, region_en = region, alias_en = alias) %>%
  mutate(web_id = coalesce(c("CASBV" = "WMN", "CASRA" = "XRA", "CASFW" = "XFW", "CASRF" = "XTI")[id], id)) %>%
  arrange(as.numeric(lon))


# write a better representation of the RadarLocation array
# new RadarLocation("Britt", "Georgian Bay", "WBI", "Ontario", new LatLon(45.79317,-80.53385),10),
pattern <- 'new RadarLocation("{name_en}", "{name_fr}", "{alias_en}", "{alias_fr}", "{region_en}", "{region_fr}", "{id}", "{web_id}", new LatLon({lat}, {lon}), {updates})'




find_assets <- function(site_id = "XTI") {
  url <- glue::glue("https://weather.gc.ca/radar/index_e.html?id={site_id}")
  nodes <- read_html(url) %>% html_nodes("#animation-frame img")
  tibble(src = html_attr(nodes, "src"), alt = html_attr(nodes, "alt")) %>%
    filter(!str_detect(alt, "Radar") | str_detect(alt, "Circles"))
}

convert_to_png <- function(src, dest) {
  magick::image_read(src) %>%
    magick::image_convert("png") %>%
    magick::image_write(dest)
  TRUE
}

assets <- locs_en %>%
  select(id, web_id) %>%
  mutate(assets = map(web_id, find_assets)) %>%
  unnest()

tmpdir <- tempfile()
dir.create(tmpdir)

assets_dl <- assets %>%
  mutate(
    url = paste0("https://weather.gc.ca", src),
    file_name = sprintf("%s_%s.gif", id, alt %>% str_to_lower() %>% str_replace(" ", "_")),
    dest_gif = file.path(tmpdir, file_name),
    dest_png = file.path(loc_assets, str_replace(file_name, "\\.gif$", ".png")),
    download = map2(url, dest_gif, curl_download),
    convert = map2(dest_gif, dest_png, convert_to_png)
  )

unlink(tmpdir, recursive = TRUE)


