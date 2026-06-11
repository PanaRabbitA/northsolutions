const fs = require('fs');

const categories = [
    { name: 'Redes', prefix: 'RED', items: ['Switch Gigabit', 'Router Empresarial', 'Cable UTP Cat 6', 'Access Point WiFi 6', 'Patch Panel 24 Puertos', 'Conector RJ45', 'Gabinete de Pared', 'Rack Abierto', 'Transceptor SFP', 'Firewall Hardware'] },
    { name: 'Telecomunicaciones', prefix: 'TEL', items: ['Teléfono IP', 'Diadema Telefónica', 'Antena Direccional', 'Radio Base', 'Central PBX', 'Gateway VoIP', 'Cable Coaxial', 'Mástil Telescópico', 'Amplificador de Señal', 'Fibra Óptica Monomodo'] },
    { name: 'Combos', prefix: 'CMB', items: ['Kit Oficina Pequeña', 'Kit Call Center', 'Combo Enlace Inalámbrico', 'Paquete Seguridad Red', 'Kit Videovigilancia IP', 'Combo Home Office', 'Paquete Red Escolar', 'Kit Fibra al Hogar', 'Combo Servidor Básico', 'Paquete Telefonía Pyme'] },
    { name: 'Software', prefix: 'SFT', items: ['Licencia Antivirus', 'Sistema Operativo Server', 'Software PBX', 'Licencia Firewall', 'Gestor de Redes', 'Software de Respaldo', 'Suite Ofimática', 'Licencia VPN', 'Monitor de Tráfico', 'Base de Datos Empresarial'] },
    { name: 'Servicios', prefix: 'SRV', items: ['Instalación de Red', 'Mantenimiento Preventivo', 'Configuración de Router', 'Certificación de Cableado', 'Auditoría de Seguridad', 'Soporte Remoto 1hr', 'Consultoría IT', 'Diseño de Red', 'Fusión de Fibra', 'Capacitación Técnica'] },
    { name: 'Sin Categoría', prefix: 'GEN', items: ['Cinchos de Plástico', 'Cinta Aislante', 'Tornillería Rack', 'Etiquetas Cables', 'Herramienta Ponchadora', 'Multímetro', 'Probador de Cables', 'Guía Pasacables', 'Organizador Vertical', 'Velcro para Cables'] }
];

let sqlAlmacen = 'USE bd_warehouse;\nSET FOREIGN_KEY_CHECKS = 0;\nDELETE FROM inventory_transactions;\nDELETE FROM products;\nSET FOREIGN_KEY_CHECKS = 1;\n\n';
sqlAlmacen += 'INSERT INTO products (id, sku, name, description, price, stock, category) VALUES\n';

const products = [];
let productId = 1;

for (let i = 0; i < categories.length; i++) {
    const cat = categories[i];
    for (let j = 1; j <= 50; j++) {
        const baseName = cat.items[j % cat.items.length];
        const name = `${baseName} Modelo ${Math.floor(Math.random() * 1000) + 100}X`;
        const sku = `${cat.prefix}-${j.toString().padStart(3, '0')}`;
        const price = (Math.random() * 5000 + 50).toFixed(2);
        const stock = Math.floor(Math.random() * 200) + 20; // 20 to 220
        
        products.push({ id: productId, sku, name, price: parseFloat(price), stock, category: cat.name });
        
        sqlAlmacen += `(${productId}, '${sku}', '${name}', 'Descripción genérica para ${name}', ${price}, ${stock}, '${cat.name}')`;
        sqlAlmacen += (productId === 300) ? ';\n' : ',\n';
        productId++;
    }
}

fs.writeFileSync('seed_almacen.sql', sqlAlmacen);

// ---------------------------
// Generate Sales (Crescendo over 5 months)
// ---------------------------
let sqlSales = 'USE bd_sales;\nSET FOREIGN_KEY_CHECKS = 0;\nDELETE FROM sale_details;\nDELETE FROM sales;\nSET FOREIGN_KEY_CHECKS = 1;\n\n';

let salesInserts = [];
let detailsInserts = [];

let saleId = 1;
let detailId = 1;

const now = new Date();
const sellers = ['admin', 'ventas'];

// Simulate 5 months. Month 1 = 5 months ago, Month 5 = current month
for (let m = 5; m >= 1; m--) {
    const date = new Date(now.getFullYear(), now.getMonth() - m + 1, 15);
    // Crescendo: more sales in more recent months
    const numSalesThisMonth = 20 + ((6 - m) * 15); // e.g. m=5 (oldest): 35, m=4: 50, ..., m=1 (now): 95
    
    for (let s = 0; s < numSalesThisMonth; s++) {
        // Random day in that month
        const saleDate = new Date(date.getTime());
        saleDate.setDate(Math.floor(Math.random() * 28) + 1);
        const dateStr = saleDate.toISOString().slice(0, 19).replace('T', ' ');
        
        const numItems = Math.floor(Math.random() * 5) + 1; // 1 to 5 items
        let total = 0;
        const currentDetails = [];
        
        for (let i = 0; i < numItems; i++) {
            const product = products[Math.floor(Math.random() * products.length)];
            const qty = Math.floor(Math.random() * 3) + 1;
            const subtotal = product.price * qty;
            total += subtotal;
            
            currentDetails.push(`(${detailId}, ${qty}, ${subtotal.toFixed(2)}, ${product.price.toFixed(2)}, ${product.id}, ${saleId})`);
            detailId++;
        }
        
        const seller = sellers[Math.floor(Math.random() * sellers.length)];
        // approx 10% requires approval
        const requiresApproval = Math.random() < 0.1 ? 1 : 0; 
        
        salesInserts.push(`(${saleId}, '${seller}', ${requiresApproval}, '${dateStr}', ${total.toFixed(2)})`);
        detailsInserts = detailsInserts.concat(currentDetails);
        
        saleId++;
    }
}

// Batch inserts for sales
sqlSales += 'INSERT INTO sales (id, created_by, requires_approval, sale_date, total) VALUES\n';
sqlSales += salesInserts.join(',\n') + ';\n\n';

sqlSales += 'INSERT INTO sale_details (id, quantity, subtotal, unit_price, product_id, sale_id) VALUES\n';
sqlSales += detailsInserts.join(',\n') + ';\n';

fs.writeFileSync('seed_ventas.sql', sqlSales);

console.log('SQL files generated successfully.');
