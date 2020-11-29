import React from 'react';
import ToiletOverviewView from "./view/ToiletOverviewView";
import CssBaseline from "@material-ui/core/CssBaseline";
import theme from "./theme";
import {Route, Switch} from "react-router-dom";
import ToiletDetailsView from "./view/ToiletDetailsView";
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
                <Route exact path="/" component={ToiletOverviewView}/>
                <Route exact path="/:toiletId" component={ToiletDetailsView}/>
            </Switch>
        </ThemeProvider>
    );
}
