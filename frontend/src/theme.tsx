import {createMuiTheme} from "@material-ui/core/styles"
import {blue} from "@material-ui/core/colors";
import {responsiveFontSizes} from "@material-ui/core";

let theme = createMuiTheme({
    palette: {
        primary: {
            main: blue[800]
        }
    }
})

theme = responsiveFontSizes(theme)

export default theme
