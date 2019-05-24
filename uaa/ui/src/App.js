import * as React from "react";
import { Theme as UWPThemeProvider, getTheme } from "react-uwp/Theme";
import { BrowserRouter, Route } from 'react-router-dom';
import SignIn from './page/SignIn';

export default class App extends React.Component {

    render(){
      return(

        <UWPThemeProvider
            style={{ width: '100%', height: '100%', position: 'relative' }}
            theme={getTheme({
            themeName: "dark", // set custom theme
            accent: "#0078D7", // set accent color
            useFluentDesign: true, // sure you want use new fluent design.
            desktopBackgroundImage: require('./res/ad3319b39fdd37b2200f8a58056dd044.jpg') // set global desktop background image
            })}
        >
          <BrowserRouter>
              <Route path="/" component={SignIn} />
              <Route path="/account" component={SignIn} />
          </BrowserRouter>
        </UWPThemeProvider>
      )
    }

}