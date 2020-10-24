import React from 'react';
import ToiletOverview from "./view/ToiletOverview";
import CssBaseline from "@material-ui/core/CssBaseline";
import theme from "./theme";
import {Route, Switch} from "react-router-dom";
import ToiletDetails from "./view/ToiletDetails";
import {ThemeProvider} from '@material-ui/core';
import NavigationBar from "./components/NavigationBar";

export default function App() {

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>

            <NavigationBar/>

            <Switch>
                <Route exact path="/" component={ToiletOverview}/>
                <Route exact path="/:toiletId" component={ToiletDetails}/>
            </Switch>
        </ThemeProvider>
    );
}
