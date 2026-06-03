const bcrypt = require('bcryptjs');

async function test() {
    const hash = "$2a$10$wE1mG1h8/r5q9aK5/r6/GOCvU33f9m6m/G.s8uT0s8P9X00V2YmUa";
    const isMatch = await bcrypt.compare("superadmin", hash);
    console.log("Matches:", isMatch);
    
    const newHash = await bcrypt.hash("superadmin", 10);
    console.log("New hash:", newHash);
}

test();
