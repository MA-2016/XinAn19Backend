import * as React from "react";
import { Theme as UWPThemeProvider, getTheme } from "react-uwp/Theme";
import { BrowserRouter, Route } from 'react-router-dom';
import SignIn from './page/SignIn';
import UserManagement from './page/UserManagement';

export default class App extends React.Component {

    render(){
      return(

        <UWPThemeProvider
            style={{ width: '100%', height: '100%', position: 'relative' }}
            theme={getTheme({
            themeName: "dark", // set custom theme
            accent: "#00a3d8", // set accent color
            useFluentDesign: true, // sure you want use new fluent design.
            desktopBackgroundImage: require('./res/b3e2a61b3cd2ab5920ebd170ded7ee7c.jpg') // set global desktop background image
            })}
        >
          <BrowserRouter>
              <Route path="/" component={UserManagement} />
              <Route path="/account" component={SignIn} />
          </BrowserRouter>
        </UWPThemeProvider>
      )
    }

}