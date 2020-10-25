import React from 'react';
import ToiletOverview from "./view/ToiletOverview";
import CssBaseline from "@material-ui/core/CssBaseline";
import theme from "./theme";
import {Route, Switch} from "react-router-dom";
import ToiletDetails from "./view/ToiletDetails";
import {ThemeProvider} from '@material-ui/core';
import NavigationBar from "./components/NavigationBar";
import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        appBarSpace: theme.mixins.toolbar,
    }),
);

export default function App() {
    const classes = useStyles();

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <NavigationBar/>
            <div className={classes.appBarSpace}/>
            <Switch>
                <Route exact path="/" component={ToiletOverview}/>
                <Route exact path="/:toiletId" component={ToiletDetails}/>
            </Switch>
        </ThemeProvider>
    );
}
