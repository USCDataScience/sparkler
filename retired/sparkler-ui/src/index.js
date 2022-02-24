import React from 'react'
import { render } from 'react-dom'
import { Provider } from 'react-redux';
import { createStore, applyMiddleware, compose } from 'redux';
import { BrowserRouter } from 'react-router-dom'
import App from './App';
import thunk from 'redux-thunk';
import {fetchAllModels} from "./actions";
import reducers from './reducers';


const store = configureStore({});
store.dispatch(fetchAllModels());

function configureStore(initialState) {
    const enhancer = compose(applyMiddleware(thunk));
    return createStore(reducers, initialState, enhancer);
}

render((
    <Provider store={store}>
    <BrowserRouter basename="/explorer">
        <App />
    </BrowserRouter>
    </Provider>
), document.getElementById('root'));