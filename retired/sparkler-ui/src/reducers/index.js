import {combineReducers} from 'redux';
import rootReducer from './test';
import searchReducer from "./search";
import modelReducer from "./model";

export default combineReducers({
    timereducer: rootReducer,
    searchreducer: searchReducer,
    modelreducer: modelReducer
});