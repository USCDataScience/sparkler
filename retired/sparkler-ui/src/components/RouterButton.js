import {Route} from "react-router-dom";
import React from "react";
import {AnchorButton} from "@blueprintjs/core";

export const RouterButton = (props) => (
    <Route
        path={props.to}
        exact={props.activeOnlyWhenExact}
        children={({match}) => (
            <AnchorButton active={match} className="button"
                          onClick={() => props.history.push(props.to)}>
                {props.label}
            </AnchorButton>
        )}
    />
);
